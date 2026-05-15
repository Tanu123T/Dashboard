import java.sql.*;

public class DescribeTable {
    public static void main(String[] args) {
        String url = "jdbc:mysql://167.235.246.36:3307/smarthrms";
        String user = "readonly_hrms";
        String password = "VGhrms@Read";

        String[] tables = {"department", "designation", "attendance", "appraisal_review", "employee_leave_account"};

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String table : tables) {
                ResultSet rs = meta.getColumns(null, null, table, null);
                System.out.println("--- " + table + " ---");
                while (rs.next()) {
                    System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
