package com.blz.assignment;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class EmployeePayrollService {

	private static Logger log = Logger.getLogger(EmployeePayrollService.class.getName());

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	private List<EmployeePayrollData> employeePayrollList;
	private Map<String, Double> employeePayrollMap;
	private EmployeePayrollDBService employeePayrollDBService;
	private EmployeePayrollDBServiceNormalised employeePayrollDBServiceNormalised;

	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
		employeePayrollDBServiceNormalised = EmployeePayrollDBServiceNormalised.getInstance();
	}

	public EmployeePayrollService(Map<String, Double> employeePayrollMap) {
		this();
		this.employeePayrollMap = employeePayrollMap;
	}

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this();
		this.employeePayrollList = new ArrayList<>(employeePayrollList);
	}

	private void readEmployeePayrollData(Scanner consoleInputReader) {
		log.info("Enter the employee ID : ");
		int id = consoleInputReader.nextInt();
		log.info("Enter the employee name : ");
		String name = consoleInputReader.next();
		log.info("Enter the employee's salary : ");
		double salary = consoleInputReader.nextDouble();

		employeePayrollList.add(new EmployeePayrollData(id, name, salary));
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			log.info("Writing Employee payroll data on Console: " + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().writeData(employeePayrollList);

	}

	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			new EmployeePayrollFileIOService().printData();
	}

	public long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new EmployeePayrollFileIOService().countEntries();
		return employeePayrollList.size();
	}

	public List<EmployeePayrollData> readPayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			this.employeePayrollList = new EmployeePayrollFileIOService().readData();
		else if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBServiceNormalised.readData();
		return employeePayrollList;
	}

	public void addEmployeePayroll(EmployeePayrollData employeePayrollData, IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary,
					employeePayrollData.startDate);
		else
			employeePayrollList.add(employeePayrollData);
	}

	public void updateEmployeeSalary(String name, double salary) {
		int result = employeePayrollDBService.updateEmployeeData(name, salary);
		if (result == 0)
			return;
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public void updateMultipleEmployeesSalary(Map<String, Double> employeeSalaryMap) {
		Map<Integer, Boolean> salaryUpdateStatus = new HashMap<>();
		employeeSalaryMap.forEach((employee, salary) -> {
			Runnable salaryUpdate = () -> {
				salaryUpdateStatus.put(employee.hashCode(), false);
				this.updateEmployeeSalary(employee, salary);
				salaryUpdateStatus.put(employee.hashCode(), true);
			};
			Thread thread = new Thread(salaryUpdate, employee);
			thread.start();
		});
		while (salaryUpdateStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

	public EmployeePayrollData getEmployeePayrollData(String name) {
		return this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name)).findFirst().orElse(null);
	}

	public List<EmployeePayrollData> readPayrollDataForRange(IOService ioService, LocalDate startDate,
			LocalDate endDate) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBServiceNormalised.getEmployeeForDateRange(startDate, endDate);
		return employeePayrollList;
	}

	public Map<String, Double> readPayrollDataForAvgSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollDBServiceNormalised.getAverageSalaryByGender();
		return employeePayrollMap;
	}

	public List<EmployeePayrollData> readPayrollDataForActiveEmployees(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollDBServiceNormalised.getActiveEmployees();
		return employeePayrollList;
	}

	public void addEmployeeToPayroll(String name, String gender, double salary, LocalDate startDate) {
		employeePayrollList.add(employeePayrollDBService.addEmployeeToPayrollUC8(name, gender, salary, startDate));
	}

	public void addEmployeeToPayrollNormalised(String name, String gender, int company_id, String company_name,
			double salary, LocalDate startDate) {
		employeePayrollList.add(employeePayrollDBServiceNormalised.addEmployeeToPayroll(name, gender, company_id,
				company_name, salary, startDate));
	}

	public void addEmployeeToPayroll(List<EmployeePayrollData> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			log.info("Employee being added : " + employeePayrollData.name);
			this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender, employeePayrollData.salary,
					employeePayrollData.startDate);
			log.info("Employee added : " + employeePayrollData.name);
		});
		log.info("" + this.employeePayrollList);
	}

	public void addEmployeeToPayrollWithThreads(List<EmployeePayrollData> employeePayrollDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employeePayrollData.hashCode(), false);
				log.info("Employee being added : " + Thread.currentThread().getName());
				this.addEmployeeToPayroll(employeePayrollData.name, employeePayrollData.gender,
						employeePayrollData.salary, employeePayrollData.startDate);
				employeeAdditionStatus.put(employeePayrollData.hashCode(), true);
				log.info("Employee added : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		log.info("" + this.employeePayrollList);
	}

	public static void main(String[] args) {
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeePayrollData(consoleInputReader);
		employeePayrollService.writeEmployeePayrollData(IOService.CONSOLE_IO);
	}

	public void updateEmployeeSalary(String name, double salary, IOService ioService) {
		if (ioService.equals(IOService.REST_IO)) {
			int result = employeePayrollDBService.updateEmployeeData(name, salary);
			if (result == 0)
				return;
		}
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public void deleteEmployeePayroll(String name, IOService ioService) {
		if (ioService.equals(IOService.DB_IO)) {
			EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
			employeePayrollList.remove(employeePayrollData);
		}
	}
}
