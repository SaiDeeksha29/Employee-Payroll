package com.blz.assignment;

import org.junit.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.Test;
import com.blz.assignment.EmployeePayrollService.IOService;

public class EmployeePayrollServiceTest {

	private static Logger log = Logger.getLogger(EmployeePayrollServiceTest.class.getName());

	@Test
	public void given3EmployeesShouldMatchEmployeeEntries() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Monica", 100000.0),
				new EmployeePayrollData(2, "Joey", 200000.0), new EmployeePayrollData(3, "Ross", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
	}

	@Test
	public void writePayrollOnFile() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Monica", 100000.0),
				new EmployeePayrollData(2, "Joey", 200000.0), new EmployeePayrollData(3, "Ross", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
	}

	@Test
	public void countEntries() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Monica", 100000.0),
				new EmployeePayrollData(2, "Joey", 200000.0), new EmployeePayrollData(3, "Ross", 300000.0) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
		long entries = employeePayrollService.countEntries(IOService.FILE_IO);
		Assert.assertEquals(3, entries);
	}

	@Test
	public void givenFileOnReadingFileShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> entries = employeePayrollService.readPayrollData(IOService.FILE_IO);
	}

	@Test
	public void givenEmployeePayrollData_ShouldMatchAverageSalary_GroupByGender() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		Map<String, Double> employeePayrollData = employeePayrollService.readPayrollDataForAvgSalary(IOService.DB_IO);
		Assert.assertEquals(2, employeePayrollData.size());
	}

	@Test
	public void givenEmployeePayrollInNormalisedDB_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollData(IOService.DB_IO);
		Assert.assertEquals(6, employeePayrollData.size());
	}

	@Test
	public void givenNewSalaryForEmployeeInNormalisedDB_WhenUpdated_ShouldSyncWithDatabase()
			throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollData(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terisa", 3000000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Terisa");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRangeForEmployeeInNormalised_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		LocalDate startDate = LocalDate.of(2018, 01, 01);
		LocalDate endDate = LocalDate.now();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readPayrollDataForRange(IOService.DB_IO,
				startDate, endDate);
		Assert.assertEquals(17, employeePayrollData.size());
	}

	@Test
	public void givenNewEmployeeInNormalised_WhenAdded_ShouldSyncWithDB() throws EmployeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayrollNormalised("Mark", "M", 3, "Capgemini", 5000000.00, LocalDate.now());
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Mark");
		Assert.assertTrue(result);
	}

	@Test
	public void givenEmployeePayrollData_ShouldReturn_ActiveEmployees() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readPayrollDataForActiveEmployees(IOService.DB_IO);
		Assert.assertEquals(16, employeePayrollData.size());
	}

	@Test
	public void givenEmployees_WhenAddedToDB_ShouldMatchEmployeeEntries() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(0, "Jeff", "M", 100000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Bill", "M", 200000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Mahesh", "M", 400000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Mukesh", "M", 300000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Sunder", "M", 500000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Anil", "M", 100000.0, LocalDate.now()) };
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readPayrollData(IOService.DB_IO);
		Instant start = Instant.now();
		employeePayrollService.addEmployeeToPayroll(Arrays.asList(arrayOfEmployee));
		Instant end = Instant.now();
		log.info("Duration without thread : " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		employeePayrollService.addEmployeeToPayrollWithThreads(Arrays.asList(arrayOfEmployee));
		Instant threadEnd = Instant.now();
		log.info("Duartion with Thread : "+Duration.between(threadStart, threadEnd));
		Assert.assertEquals(18, employeePayrollService.countEntries(IOService.DB_IO));
	}

}
