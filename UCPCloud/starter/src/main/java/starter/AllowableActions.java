package starter;


public enum AllowableActions {

    CAN_DELETE_DOCUMENT("can_delete_document"),
    CAN_UPDATE_PROPERTIES("can_update_properties"),
    CAN_APPLY_ACL("can_apply_acl"),
    CAN_APPLY_STREAM("can_delete_stream"),
    CAN_DOWNLOAD_STREAM("can_download_stream");

    private String value;

    private AllowableActions(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

//    *********CMIS**********
//    CAN_ADD_OBJECT_TO_FOLDER,
//    CAN_APPLY_ACL,
//    CAN_APPLY_POLICY,
//    CAN_CANCEL_CHECK_OUT,
//    CAN_CHECK_IN,
//    CAN_CHECK_OUT,
//    CAN_CREATE_DOCUMENT,
//    CAN_CREATE_FOLDER,
//    CAN_CREATE_ITEM,
//    CAN_CREATE_RELATIONSHIP,
//    CAN_DELETE_CONTENT_STREAM,
//    CAN_DELETE_OBJECT,
//    CAN_DELETE_TREE,
//    CAN_GET_ACL,
//    CAN_GET_ALL_VERSIONS,
//    CAN_GET_APPLIED_POLICIES,
//    CAN_GET_CHILDREN,
//    CAN_GET_CONTENT_STREAM,
//    CAN_GET_DESCENDANTS,
//    CAN_GET_FOLDER_PARENT,
//    CAN_GET_FOLDER_TREE,
//    CAN_GET_OBJECT_PARENTS,
//    CAN_GET_OBJECT_RELATIONSHIPS,
//    CAN_GET_PROPERTIES,
//    CAN_GET_RENDITIONS,
//    CAN_MOVE_OBJECT,
//    CAN_REMOVE_OBJECT_FROM_FOLDER,
//    CAN_REMOVE_POLICY,
//    CAN_SET_CONTENT_STREAM,
//    CAN_UPDATE_PROPERTIES,
// ************************/
}
