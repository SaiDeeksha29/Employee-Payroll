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
	public void givenListOfNewEmployees_WhenAdded__ShouldMatch() {
		EmployeePayrollService employeePayrollService;
		EmployeePayrollData[] arrayOfEmployees = getEmployeeList();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployees));
		EmployeePayrollData[] arrayOfEmployeePayrolls = {
				new EmployeePayrollData(0, "Sathya", "M", 4000000.00, LocalDate.now()),
				new EmployeePayrollData(0, "Chandler", "M", 3000000.00, LocalDate.now()),
				new EmployeePayrollData(0, "Joey", "M", 2000000.00, LocalDate.now()) };
		for (EmployeePayrollData employeePayrollData : arrayOfEmployeePayrolls) {
			Response response = addEmployeeToJsonServer(employeePayrollData);
			int statusCode = response.getStatusCode();
			Assert.assertEquals(201, statusCode);
			employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
			employeePayrollService.addEmployeePayroll(employeePayrollData, IOService.REST_IO);
		}
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, entries);
	}
}
