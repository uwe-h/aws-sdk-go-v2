package software.amazon.smithy.aws.go.codegen.customization.service.s3;

import static software.amazon.smithy.aws.go.codegen.customization.service.s3.S3ModelUtils.isServiceS3;

import java.util.List;
import java.util.stream.Collectors;

import software.amazon.smithy.aws.go.codegen.XmlProtocolUtils;
import software.amazon.smithy.codegen.core.CodegenException;
import software.amazon.smithy.codegen.core.Symbol;
import software.amazon.smithy.codegen.core.SymbolProvider;
import software.amazon.smithy.go.codegen.*;
import software.amazon.smithy.aws.go.codegen.ResponsePositionTrait;
import software.amazon.smithy.aws.go.codegen.TransientForDeserializationTrait;
import software.amazon.smithy.go.codegen.integration.*;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.TopDownIndex;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.traits.*;
import software.amazon.smithy.model.transform.ModelTransformer;
import software.amazon.smithy.utils.ListUtils;

/**
 * Restrictions around timestamp formatting for the 'Expires' value in some S3 responses has never been standardized and
 * thus many non-conforming values for the field (unsupported formats, arbitrary strings, etc.) exist in the wild. This
 * customization makes the response parsing forgiving for this field in responses and adds an ExpiresString field that
 * contains the unparsed value.
 */
public class S3ListOjectVersionsSortOrder implements GoIntegration {
    private static final ShapeId S3_LIST_OBJECT_VERSIONS = ShapeId.from("com.amazonaws.s3#ListObjectVersions");
    private static final String S3_LIST_OBJECT_VERSIONS_OP_ID ="ListObjectVersions";
    private static final ShapeId S3_LIST_OBJECT_VERSIONS_OUTPUT = ShapeId.from("smithy.go.synthetic#ListObjectVersionsOutput");
    private static final ShapeId S3_SORT_ORDER = ShapeId.from("com.amazonaws.s3#SortOrder");
    private static final ShapeId S3_SORT_ORDER_INTEGER = ShapeId.from("com.amazonaws.s3#SortOrderInteger");
    private static final ShapeId S3_DELETE_MARKER = ShapeId.from("com.amazonaws.s3#DeleteMarkerEntry");
    private static final ShapeId S3_VERSION = ShapeId.from("com.amazonaws.s3#ObjectVersion");
    private static final ResponsePositionTrait RESPONSE_POSITION_TRAIT = new ResponsePositionTrait("SortOrder", "int32(0)", List.of("DeleteMarkers", "Versions"));



    @Override
    public Model preprocessModel(Model model, GoSettings settings) {
        if (!isServiceS3(model, settings.getService(model))) {
            return model;
        }


        var withSortOrderInt = model.toBuilder()
                .addShape(IntegerShape.builder()
                        .id(S3_SORT_ORDER_INTEGER)
                        .build())
                .build();
        return ModelTransformer.create().mapShapes(withSortOrderInt, this::addSortOrder);
    }

    private Shape addSortOrder(Shape shape) {
        if(!shape.isStructureShape()) {
            return shape;
        }

        if(shape.getId().equals(S3_LIST_OBJECT_VERSIONS_OUTPUT)) {
            System.out.println("Adding Response_position Trait: "+shape.getAllTraits().keySet());
            var res = Shape.shapeToBuilder(shape)
                    .addTrait(RESPONSE_POSITION_TRAIT);
            for(var trait : shape.getAllTraits().values()) {
                res = res.addTrait(trait);
            }
            return res.build();
        }

        if (!shape.getId().equals(S3_DELETE_MARKER) && !shape.getId().equals(S3_VERSION)) {
            return shape;
        } 

        
        System.out.println("Adding SortOrder: "+shape.getId().getName());
        var stringDocs = new DocumentationTrait("The sort order can be used for ListObjectVersions to bring " +
                " the DeleteMarkers and ObjectVersions in a chronical order. If (ov1|dm1).SortOrder > (ov2|dm2) " +
                "then (ov1|dm1).LastModified >=  (ov2|dm2).LastModified)");

        return Shape.shapeToBuilder(shape)
                .addMember(MemberShape.builder()
                        .id(shape.getId()
                                    .withMember(S3_SORT_ORDER.getName()))
                        .target(S3_SORT_ORDER_INTEGER)
                                   .addTrait(new TransientForDeserializationTrait())
                        .addTrait(stringDocs)
                        .build())
                .build();
    }

}
