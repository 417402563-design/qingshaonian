package cn.edu.sdua.qingshaonian.entity;

/**
 * 系统角色常量：青少年学生 / 心理咨询师 / 管理员
 */
public final class Role {

    public static final String STUDENT = "STUDENT";
    public static final String COUNSELOR = "COUNSELOR";
    public static final String ADMIN = "ADMIN";

    private Role() {
    }

    public static boolean isValid(String role) {
        return STUDENT.equals(role) || COUNSELOR.equals(role) || ADMIN.equals(role);
    }

    public static String labelOf(String role) {
        return switch (role == null ? "" : role) {
            case STUDENT -> "青少年用户";
            case COUNSELOR -> "心理咨询师";
            case ADMIN -> "管理员";
            default -> role;
        };
    }
}
