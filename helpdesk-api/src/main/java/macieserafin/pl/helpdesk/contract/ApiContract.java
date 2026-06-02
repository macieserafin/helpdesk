package macieserafin.pl.helpdesk.contract;

public final class ApiContract {
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 100;
    public static final int ROLE_NAME_MAX_LENGTH = 30;

    public static final int PROFILE_FIRST_NAME_MAX_LENGTH = 50;
    public static final int PROFILE_LAST_NAME_MAX_LENGTH = 50;
    public static final int PROFILE_PHONE_NUMBER_MAX_LENGTH = 30;
    public static final int PROFILE_CITY_MAX_LENGTH = 100;
    public static final int PROFILE_STREET_ADDRESS_MAX_LENGTH = 150;
    public static final int PROFILE_POSTAL_CODE_MAX_LENGTH = 20;

    public static final int TICKET_TITLE_MAX_LENGTH = 150;
    public static final int TICKET_DESCRIPTION_MAX_LENGTH = 4000;
    public static final int TICKET_CATEGORY_MAX_LENGTH = 100;
    public static final int TICKET_ENUM_MAX_LENGTH = 30;

    public static final int CATEGORY_NAME_MAX_LENGTH = 100;
    public static final int CATEGORY_DESCRIPTION_MAX_LENGTH = 1000;
    public static final int COMMENT_CONTENT_MAX_LENGTH = 2000;

    public static final int ATTACHMENT_FILE_NAME_MAX_LENGTH = 255;
    public static final int ATTACHMENT_FILE_PATH_MAX_LENGTH = 500;
    public static final int ATTACHMENT_CONTENT_TYPE_MAX_LENGTH = 100;

    private ApiContract() {
    }
}
