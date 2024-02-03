package hex.multinode.storage.integration.rest;

import hex.multinode.storage.model.data.MultiNode;
import hex.multinode.storage.service.NodeManager;
import hex.multinode.storage.model.dto.NodeDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(path = "api/v1/nodes")
public class NodeController {

    private final NodeManager nodeManager;

    @Autowired
    public NodeController(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @GetMapping("/{id}")
    public MultiNode findNodeById(@PathVariable String id) {
        return nodeManager.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/find")
    public List<MultiNode> findNodesByTitle(@RequestParam @NotBlank() String title) {
        return nodeManager.findNodesByTitle(title);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MultiNode createNode(@Valid @RequestBody NodeDTO nodeDTO) {
        return nodeManager.save(nodeDTO);
    }

    @PatchMapping
    public MultiNode editNode(@Valid @RequestBody NodeDTO nodeDTO) {
        try {
            return nodeManager.update(nodeDTO);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public MultiNode deleteNode(@PathVariable @NotBlank String id) {
        try {
            return nodeManager.deleteById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

}
