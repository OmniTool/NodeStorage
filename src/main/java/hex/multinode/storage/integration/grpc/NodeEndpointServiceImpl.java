package hex.multinode.storage.integration.grpc;

import com.example.hex.integration.grpc.*;
import com.google.rpc.Code;
import com.google.rpc.Status;
import hex.multinode.storage.aspect.GrpcRqToLog;
import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.model.dto.NodeDTO;
import hex.multinode.storage.service.NodeManager;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@GRpcService
@Log4j2
public class NodeEndpointServiceImpl extends NodeEndpointServiceGrpc.NodeEndpointServiceImplBase {

    private final NodeManager<MultiNode> nodeManager;

    @Autowired
    public NodeEndpointServiceImpl(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    @GrpcRqToLog
    public void createNode(CreateNodeRequest request, StreamObserver<CreateNodeResponse> responseObserver) {
        BiConsumer<CreateNodeRequest, StreamObserver<CreateNodeResponse>> consumer = (rq, rsObserver) -> {
            MultiNode node = nodeManager.save(NodeDTO.of(rq.getTitle(), rq.getText()));
            CreateNodeResponse response = CreateNodeResponse.newBuilder()
                    .setNode(buildNodeProto(node))
                    .build();
            sendResponse(response, rsObserver);
        };
        wrapExceptions(consumer, request, responseObserver);
    }

    @Override
    @GrpcRqToLog
    public void deleteNodeById(DeleteNodeByIdRequest request, StreamObserver<DeleteNodeByIdResponse> responseObserver) {
        BiConsumer<DeleteNodeByIdRequest, StreamObserver<DeleteNodeByIdResponse>> consumer = (rq, rsObserver) -> {
            MultiNode node = nodeManager.deleteById(rq.getId());
            DeleteNodeByIdResponse response = DeleteNodeByIdResponse.newBuilder()
                    .setNode(buildNodeProto(node))
                    .build();
            sendResponse(response, rsObserver);
        };
        wrapExceptions(consumer, request, responseObserver);
    }

    @Override
    @GrpcRqToLog
    public void findNodesByTitle(FindNodesByTitleRequest request, StreamObserver<FindNodesByTitleResponse> responseObserver) {
        BiConsumer<FindNodesByTitleRequest, StreamObserver<FindNodesByTitleResponse>> consumer = (rq, rsObserver) -> {
            List<MultiNode> nodes = nodeManager.findNodesByTitle(rq.getTitle());
            FindNodesByTitleResponse response = FindNodesByTitleResponse.newBuilder()
                    .addAllNode(buildNodesProto(nodes))
                    .build();
            sendResponse(response, rsObserver);
        };
        wrapExceptions(consumer, request, responseObserver);
    }

    @Override
    @GrpcRqToLog
    public void findNodeById(FindNodeByIdRequest request, StreamObserver<FindNodeByIdResponse> responseObserver) {
        BiConsumer<FindNodeByIdRequest, StreamObserver<FindNodeByIdResponse>> consumer = (rq, rsObserver) -> {
            nodeManager.findById(rq.getId())
                    .ifPresentOrElse(node -> {
                                FindNodeByIdResponse response = FindNodeByIdResponse.newBuilder()
                                        .setNode(buildNodeProto(node))
                                        .build();
                                sendResponse(response, rsObserver);
                            },
                            () -> {
                                Status status = Status.newBuilder()
                                        .setCode(Code.NOT_FOUND_VALUE)
                                        .build();
                                rsObserver.onError(StatusProto.toStatusRuntimeException(status));
                            });
        };
        wrapExceptions(consumer, request, responseObserver);
    }

    private <Rq, Rs> void wrapExceptions(BiConsumer<Rq, StreamObserver<Rs>> consumer, Rq request, StreamObserver<Rs> responseObserver) {
        try {
            consumer.accept(request, responseObserver);
        } catch (NoSuchElementException e) {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage(e.getMessage())
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        } catch (Exception e) {
            Status status = Status.newBuilder()
                    .setCode(Code.INTERNAL_VALUE)
                    .setMessage(e.getMessage())
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    private <Rs> void sendResponse(Rs response, StreamObserver<Rs> responseObserver) {
        log.info("gRPC response: " + response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static NodeProto buildNodeProto(MultiNode node) {
        return NodeProto.newBuilder()
                .setId(node.getId().toString())
                .setTitle(node.getTitle())
                .setText(node.getContent().getText())
                .build();
    }

    private static List<NodeProto> buildNodesProto(List<MultiNode> nodes) {
        return nodes.stream().map(NodeEndpointServiceImpl::buildNodeProto).toList();
    }
}
