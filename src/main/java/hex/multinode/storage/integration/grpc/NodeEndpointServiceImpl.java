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
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;

@GRpcService
@Slf4j
public class NodeEndpointServiceImpl extends NodeEndpointServiceGrpc.NodeEndpointServiceImplBase {

    private final NodeManager nodeManager;

    @Autowired
    public NodeEndpointServiceImpl(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    @GrpcRqToLog
    public void createNode(CreateNodeRequest request, StreamObserver<CreateNodeResponse> responseObserver) {
        MultiNode node = nodeManager.save(NodeDTO.of(request.getTitle(), request.getText()));
        CreateNodeResponse response = CreateNodeResponse.newBuilder()
                .setNode(buildNodeProto(node))
                .build();
        sendResponse(response, responseObserver);
    }

    @Override
    @GrpcRqToLog
    public void findNodeById(FindNodeByIdRequest request, StreamObserver<FindNodeByIdResponse> responseObserver) {
        nodeManager.findById(request.getId())
                .ifPresentOrElse(node -> {
                            FindNodeByIdResponse response = FindNodeByIdResponse.newBuilder()
                                    .setNode(buildNodeProto(node))
                                    .build();
                            sendResponse(response, responseObserver);
                        },
                        () -> {
                            Status status = Status.newBuilder()
                                    .setCode(Code.NOT_FOUND_VALUE)
                                    .build();
                            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
                        });
    }

    @Override
    @GrpcRqToLog
    public void deleteNodeById(DeleteNodeByIdRequest request, StreamObserver<DeleteNodeByIdResponse> responseObserver) {
        try {
            MultiNode node = nodeManager.deleteById(request.getId());
            DeleteNodeByIdResponse response = DeleteNodeByIdResponse.newBuilder()
                    .setNode(buildNodeProto(node))
                    .build();
            sendResponse(response, responseObserver);
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

    @Override
    @GrpcRqToLog
    public void findNodesByTitle(FindNodesByTitleRequest request, StreamObserver<FindNodesByTitleResponse> responseObserver) {
        List<MultiNode> nodes = nodeManager.findNodesByTitle(request.getTitle());
        FindNodesByTitleResponse response = FindNodesByTitleResponse.newBuilder()
                .addAllNode(buildNodesProto(nodes))
                .build();
        sendResponse(response, responseObserver);
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
