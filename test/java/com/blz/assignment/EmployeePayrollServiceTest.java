package com.blz.assignment;

import org.junit.Assert;
import java.util.Arrays;
import org.junit.Test;

import com.blz.assignment.EmployeePayrollService.IOService;

public class EmployeePayrollServiceTest {

	@Test
	public void gievn3EmployeesShouldMatchEmployeeEntries() {
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(1, "Monica", 100000.0),
				new EmployeePayrollData(2, "Joey", 200000.0), new EmployeePayrollData(3, "Ross", 300000.0) };
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmployee));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
		employeePayrollService.printData(IOService.FILE_IO);
		long entries = employeePayrollService.countEntries(IOService.FILE_IO);
		Assert.assertEquals(3, entries);
	}

}
