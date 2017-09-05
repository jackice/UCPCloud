package starter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2015/8/13.
 */
public class Constant {

    /**
     * 权限
     */
    public enum Permission{
        read(1),
        write(2);

        private int value = -1;

        Permission(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Permission fromValue(int value) {
            switch (value){
                case 1:
                    return read;
                case 2:
                    return write;
                default:
                    throw new RuntimeException("No type match for [" + value + "]");
            }
        }

        public static Permission fromString(String permission) {
            String lowersOpType = permission.toLowerCase(Locale.ROOT);
            switch (lowersOpType) {
                case "read":
                    return Permission.read;
                case "write":
                    return Permission.write;
                default:
                    throw new RuntimeException("permission [" + permission + "] not allowed, either [read] or [write] are allowed");
            }
        }

        public static List<String> getPermissionDeclaration(){
            List<String> declaration = new ArrayList<>();
            declaration.add("read");
            declaration.add("write");
            return declaration;
        }
    }

    public static final String SYSTEM = "ucp_system";
    public static final String LOG_INDEX = "ucp_log";
    public static final String ADMIN = "admin";
    public static final String ADMINGROUP = "adminGroup";
    public static final String EVERYONE = "everyone";
    public static final String DEFAULTPASSWORD = "123456";

    public static final String DEFAULT_INDEXANALYZER = "ansj_index";
    public static final String DEFAULT_SEARCHANALYZER = "ansj_query";

    public static final String INDICES_SUFFIX = "_v0";

    public static final String LOG_TYPE_NAME = "logInfo";
    public static final String LOG_INDDX_NAME = "ucp_log";

    public static final String SUGNATURE = "signature";
    /**
     * ES中mapping定义相关字段名称
     */
    public class FieldName {

        public static final String meta = "_meta";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String CREATEDBY = "createdBy";
        public static final String CREATEDON = "createdOn";
        public static final String LASTUPDATEDBY = "lastUpdatedBy";
        public static final String LASTUPDATEDON = "lastUpdatedOn";
        public static final String TAG = "tag";
        public static final String ACL = "_acl";
        public static final String STREAMS = "_streams";
        public static final String STREAMID = "streamId";
        public static final String STREAMNAME = "streamName";
        public static final String CONTENTTYPE = "contentType";
        public static final String PAGECOUNT = "pageCount";
        public static final String LENGTH = "length";
        public static final String FULLTEXT = "_fullText";
        public static final String REQUIRED = "required";
        public static final String TYPE = "type";
        public static final String DEFAULTVALUE = "defaultValue";
        public static final String ORDER = "order";
        public static final String PATTERN = "pattern";
        public static final String PROMPTMESSAGE = "promptMessage";
        public static final String USER = "user";
        public static final String GROUP = "group";
        public static final String PERMISSION = "permission";
        public static final String ALLOWABLEACTIONS = "_allowableActions";
        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String GROUPS = "groups";

        public static final String ISDOCTYPE = "isDocType";
        public static final String DISPLAYNAME = "displayName";
        public static final String DESCRIPTION = "description";
        public static final String PROPERTIES = "properties";
        public static final String ISDELETED = "isDeleted";


        public static final String INDEX = "index";
        public static final String ANALYZED = "analyzed";
        public static final String NOT_ANALYZED = "not_analyzed";

        public static final String INDEXANALYZER = "indexAnalyzer";
        public static final String SEARCHANALYZER = "searchAnalyzer";
        public static final String ANALYZER = "analyzer";

        public static final String _ID = "_id";

        //fields of user
        public static final String USERTYPENAME = "user";
        public static final String USERID = "userId";
        public static final String USERNAME = "userName";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";

        //fields of group
        public static final String GROUPTYPENAME = "group";
        public static final String GROUPID = "groupId";
        public static final String GROUPNAME = "groupName";
        public static final String USERS = "users";
        public static final String CHILDGROUPS = "childGroups";

        //fields of VIEW
        public static final String VIEWID = "viewId";
        public static final String VIEWNAME = "viewName";
        public static final String VIEWTYPENAME = "view";
        public static final String QUERYCONTEXT = "queryContext";

        //fields of tag
        public static final String TAGID = "tagId";
        public static final String TAGCONTEXT = "tagContext";
        public static final String TAGTYPENAME = "tag";


    }


    /**
     * 属性值的类型
     */
    public class Type{
        public static final String STRING = "String";
        public static final String INTEGER = "integer";
        public static final String FLOAT = "float";
        public static final String DATE = "date";
        public static final String BOOLEAN = "boolean";
    }


}
