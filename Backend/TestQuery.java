import java.sql.*;

public class TestQuery {
    public static void main(String[] args) {
        String url = "jdbc:mysql://167.235.246.36:3307/smarthrms";
        String user = "readonly_hrms";
        String password = "VGhrms@Read";

        String query = "select e1_0.id,e1_0.branch_id,e1_0.company_id,e1_0.department_id,e1_0.designation_id,e1_0.email_id,e1_0.emp_unique_id,e1_0.status,e1_0.employment_type_id,e1_0.first_name,e1_0.gender,e1_0.employment_date,e1_0.last_name,e1_0.last_modified,e1_0.last_modified_by,e1_0.reporting_emp_id from employee e1_0 limit 0, 20";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("Query executed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
