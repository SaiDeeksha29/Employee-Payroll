package com.blz.assignment;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollDBServiceNormalised {

	private static EmployeePayrollDBServiceNormalised employeePayrollDBServiceNormalised;
	private PreparedStatement employeePayrollDataStatementNormalised;

	private EmployeePayrollDBServiceNormalised() {

	}

	public static EmployeePayrollDBServiceNormalised getInstance() {
		if (employeePayrollDBServiceNormalised == null)
			employeePayrollDBServiceNormalised = new EmployeePayrollDBServiceNormalised();
		return employeePayrollDBServiceNormalised;
	}

	public List<EmployeePayrollData> readData() {
		String sql = "SELECT e.id,e.company_Id,e.name,e.gender,e.start,e.company_Name,d.Department_Name,p.basic_pay "
				+ "FROM employee_payroll2 e JOIN employee_department2 d2 " + "ON e.id = d2.ID " + "JOIN department2 d "
				+ "ON d2.Department_ID  = d.Department_ID  " + "JOIN payroll_details p " + "ON e.id = p.employee_id;";
		return this.getEmployeePayrollDataUsingSQLQuery(sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingSQLQuery(String sql) {
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		List<String> department = new ArrayList<String>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String companyName = resultSet.getString("company_Name");
				String dept = resultSet.getString("department_name");
				double salary = resultSet.getDouble("basic_pay");
				department.add(dept);
				String[] departmentArray = new String[department.size()];
				employeePayrollList.add(new EmployeePayrollData(id, name, gender, salary, startDate, companyName,
						companyId, department.toArray(departmentArray)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollDataStatementNormalised == null)
			this.preparedStatementForEmployeeData();
		try {
			employeePayrollDataStatementNormalised.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatementNormalised.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void preparedStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "SELECT e.id,e.company_id,e.name,e.gender,e.start,e.company_Name,d.Department_Name,p.basic_pay "
					+ "FROM employee_payroll2 e JOIN employee_department2 d2 " + "ON e.id = d2.ID "
					+ "JOIN department2 d " + "ON d2.Department_ID = d.Department_ID " + "JOIN payroll_details p "
					+ "ON e.id = p.employee_id WHERE e.name = ?";

			employeePayrollDataStatementNormalised = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int updateEmployeeData(String name, Double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	private int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		String sql = String.format("UPDATE payroll_details SET basic_pay = %.2f WHERE employee_id = "
				+ "(SELECT id from employee_payroll2 WHERE name = '%s');", salary, name);
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			return prepareStatement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<EmployeePayrollData> getEmployeeForDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format(
				"SELECT * FROM employee_payroll2 WHERE start BETWEEN CAST('2018-01-01' AS DATE) AND DATE(NOW());",
				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getEmployeePayrollDataUsingDB(sql);
	}

	private List<EmployeePayrollData> getEmployeePayrollDataNormalised(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String companyName = resultSet.getString("company_Name");
				double salary = resultSet.getDouble("salary");
				employeePayrollList
						.add(new EmployeePayrollData(id, name, gender, salary, startDate, companyName, companyId));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) {
		ResultSet resultSet;
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollDataNormalised(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public Map<String, Double> getAverageSalaryByGender() {
		String sql = "SELECT gender,AVG(salary) as avg_salary FROM employee_payroll2 GROUP BY gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToAverageSalaryMap;
	}

	public EmployeePayrollData addEmployeeToPayroll(String name, String gender, int company_id, String company_name,
			double salary, LocalDate startDate) {
		int employeeId = -1;
		Connection connection = null;
		EmployeePayrollData employeePayrollData = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO employee_payroll2(name,gender,company_Id,company_Name,salary,start) VALUES ('%s','%s','%s','%s','%s','%s');",
					name, gender, company_id, company_name, salary, Date.valueOf(startDate));
			int rowAffected = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(employeeId, name, gender, company_id, company_name, salary,
					startDate);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return employeePayrollData;
		}

		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"INSERT INTO payroll_details(employee_id,basic_pay,deductions,taxable_pay,tax ,net_pay)VALUES (%s,%s,%s,%s,%s,%s)",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId, name, salary, startDate);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return employeePayrollData;
		}
		try (Statement statement = connection.createStatement()) {
			int departmentId = 24;
			String departmentName = "ECE";
			String sql = String.format("INSERT INTO department2(Department_ID,Department_Name) VALUES ('%s','%s')",
					departmentId, departmentName);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				employeePayrollData = new EmployeePayrollData(departmentId, departmentName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return employeePayrollData;
		}
		try (Statement statement = connection.createStatement()) {
			int departmentId = 24;
			String sql = String.format("INSERT INTO employee_department2(id,Department_ID) VALUES (%s,%s)", employeeId,
					departmentId);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1) {
				employeePayrollData = new EmployeePayrollData(employeeId, departmentId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			return employeePayrollData;
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return employeePayrollData;
	}
	
	public List<EmployeePayrollData> getActiveEmployees() {
		String sql = "select * from employee_payroll2 where is_active=1;";
		return this.getEmployeePayrollDataUsingDBActive(sql);
	}
	
	private List<EmployeePayrollData> getEmployeePayrollDataNormalisedActive(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int companyId = resultSet.getInt("company_id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String companyName = resultSet.getString("company_Name");
				double salary = resultSet.getDouble("salary");
				boolean is_active=resultSet.getBoolean("is_active");
				employeePayrollList
						.add(new EmployeePayrollData(id, name, gender, salary, startDate, companyName, companyId, is_active));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}
	
	private List<EmployeePayrollData> getEmployeePayrollDataUsingDBActive(String sql) {
		ResultSet resultSet;
		List<EmployeePayrollData> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollDataNormalisedActive(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "satyasai1";
		Connection connection;
		System.out.println("Connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Connection successful: " + connection);
		return connection;
	}

}
