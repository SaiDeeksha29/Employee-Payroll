package com.blz.assignment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.*;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.blz.assignment.EmployeePayrollService.IOService;

public class EmployeePayrollJsonServerTest {

	private static Logger log = Logger.getLogger(EmployeePayrollJsonServerTest.class.getName());

	@Before
	public void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	public EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		log.info("Employee payroll entries in JSON Server :\n" + response.asString());
		EmployeePayrollData[] arrayOfEmployees = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmployees;
	}

	public Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrived_ShouldMatchCount() {
		EmployeePayrollData[] arrayOfEmps = getEmployeeList();
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}

	@Test
	public void givenEmployeeToDelete_WhenDeleted_ShouldMatchCount() {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployees = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		EmployeePayrollData employeePayrollData = employeePayrollService.getEmployeePayrollData("Sathya");
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		Response response = request.delete("/employees/" + employeePayrollData.id);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
		employeePayrollService.deleteEmployeePayroll(employeePayrollData.name, IOService.REST_IO);
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(5, entries);
	}
}
