package software.amazon.smithy.aws.go.codegen;

import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;

import java.util.List;

public class ResponsePositionTrait extends AnnotationTrait {
    public static final ShapeId ID = ShapeId.from("smithy.api#sortOrder");
    private final String sortOrderMemberName;
    private final String initValue;
    private final List<String> membersWithSortOrder;

    private static ObjectNode createNode(String targetMemberName, String initValue, List<String> targetContainerNames) {
        var targetsNode = ArrayNode.builder();
        for(var tar : targetContainerNames) {
            targetsNode = targetsNode.withValue(tar);
        }
        return ObjectNode.builder()
        .withMember("targetMemberName", targetMemberName)
                .withMember("initValue", initValue)
                .withMember("targetContainerNames", targetsNode.build())
                .build();

    }
    public ResponsePositionTrait(String sortOrderMemberName,
                                 String initValue,
                                 List<String> membersWithSortOrder) {
        super(ID, ResponsePositionTrait.createNode(sortOrderMemberName,
                                                   initValue,
                                                   membersWithSortOrder));
        this.sortOrderMemberName = sortOrderMemberName;
        this.membersWithSortOrder = membersWithSortOrder;
        this.initValue = initValue;
    }

    public String getSortOrderMemberName() {
        return sortOrderMemberName;
    }

    public List<String> getMembersWithSortOrder() {
        return membersWithSortOrder;
    }

    public String getInitValue() {
        return initValue;
    }
}
