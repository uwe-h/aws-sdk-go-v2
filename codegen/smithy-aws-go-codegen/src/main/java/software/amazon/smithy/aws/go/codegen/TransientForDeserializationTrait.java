package software.amazon.smithy.aws.go.codegen;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;

public class TransientForDeserializationTrait extends AbstractTrait{
    public static final ShapeId ID = ShapeId.from("smithy.api#transient");

    public TransientForDeserializationTrait() {
        super(ID, SourceLocation.NONE);
    }


    @Override
    protected Node createNode() {
        return Node.objectNodeBuilder()
                .withMember("ID", ID.getName())
                .build();
    }
}
