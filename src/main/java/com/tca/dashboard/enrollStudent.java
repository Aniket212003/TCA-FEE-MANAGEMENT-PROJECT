package com.tca.dashboard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class enrollStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public enrollStudent() {
        super();
       
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html");
		//PrintWriter out = response.getWriter();
		try 
		{
			Long whatsapp_num = Long.parseLong(request.getParameter("wp-number")); 
			Integer course_code = Integer.parseInt(request.getParameter("course"));
			String fee_type = request.getParameter("fee-type");
			Double amount_in_num = Double.parseDouble(request.getParameter("amount-in-num"));
			String enrollment_date = request.getParameter("date");
			String payment_type = request.getParameter("pay");
			Integer student_no = 0;
			String name_stud = "";
			Date sqlDate = Date.valueOf(enrollment_date);
			String option = request.getParameter("Submit");
			
			Connection conn=null;
			PreparedStatement ps=null;
			ResultSet rs = null;
		
			String dbUrl="jdbc:postgresql://localhost/tca_project";
			String user="aniket";
			String password="212003";
		
			try 
			{
				Class.forName("org.postgresql.Driver");
				conn=DriverManager.getConnection(dbUrl,user,password);
				
				ps = conn.prepareStatement("select student_whatsapp_num from student_fee_details where student_whatsapp_num = ?;");
				ps.setLong(1,whatsapp_num);
				rs = ps.executeQuery();
				Long wp = null;
				while(rs.next())
				{
					wp = rs.getLong(1);
				}
		
				ps = conn.prepareStatement("select course_fee from course_details where course_code = ?;");
				ps.setInt(1, course_code);
				rs = ps.executeQuery();
				Double dbCourseFee = null;
				while(rs.next())
				{
					dbCourseFee = rs.getDouble(1);
				}
				
				ps = conn.prepareStatement("select student_no, student_full_name from student_fee_details where student_whatsapp_num = ?;");
				ps.setLong(1,whatsapp_num);
				rs = ps.executeQuery();

				while(rs.next())
				{
						student_no = rs.getInt(1);
						name_stud = rs.getString(2);
				}
				
				if(option.equals("Enroll Student"))
				{
									
						if(wp != null && amount_in_num <= dbCourseFee && amount_in_num > 0.0)
						{

								ps = conn.prepareStatement("select student_no, student_full_name from student_fee_details where student_whatsapp_num = ?;");
								ps.setLong(1,whatsapp_num);
								rs = ps.executeQuery();
				
								while(rs.next())
								{
										student_no = rs.getInt(1);
										name_stud = rs.getString(2);
								}			
					
								ps = conn.prepareStatement("insert into student_course_details values (?,?,?,?,?,?);");
										
								ps.setInt(1,student_no);
								ps.setInt(2,course_code);
								ps.setString(3, fee_type);
								ps.setDouble(4, amount_in_num);
								ps.setDate(5, sqlDate);
								ps.setString(6, payment_type);
			
								ps.executeUpdate();
			
								String message = "Student : " + name_stud + " Enrolled For : " + course_code;
								request.setAttribute("message", message);
						}
						else
						{
								if(wp==null)
								{
										String doesNotExist = "Whatsapp Number Does Not Exist !!";
										request.setAttribute("error", doesNotExist);
								}
								else if(amount_in_num > dbCourseFee || amount_in_num < 0.0)
								{
										String invalidAmount = "Amount Invalid !!";
										request.setAttribute("error", invalidAmount);
								}
						}
				}
				else if(option.equals("Update Fee"))
				{
					if(wp != null && amount_in_num <= dbCourseFee && amount_in_num > 0.0)
					{
						ps = conn.prepareStatement("Select amount_in_num from student_course_details where student_no = ? AND course_code = ?;");
						ps.setInt(1,student_no);
						ps.setInt(2,course_code);
						rs = ps.executeQuery();
						Double existingAmount = null;
						while(rs.next())
						{
							existingAmount = rs.getDouble(1);
						}
						
						
						ps = conn.prepareStatement("UPDATE student_course_details SET amount_in_num = ?, student_fee_type = ?, enrollment_date = ?, payment_type = ? WHERE student_no = ? AND course_code = ? RETURNING amount_in_num; ");
						ps.setDouble(1, existingAmount + amount_in_num);
						ps.setString(2, fee_type);
						ps.setDate(3, sqlDate);
						ps.setString(4, payment_type);
						ps.setInt(5, student_no);
						ps.setInt(6,course_code);
						
						rs = ps.executeQuery();
						Double updatedAmount = null;
						
						while(rs.next())
						{
							updatedAmount = rs.getDouble(1);
						}
						String message = "Student : " + name_stud + " Fee Updated : " + updatedAmount;
						request.setAttribute("message", message);
					}
					else
					{
							if(wp==null)
							{
									String doesNotExist = "Whatsapp Number Does Not Exist !!";
									request.setAttribute("error", doesNotExist);
							}
							else if(amount_in_num > dbCourseFee || amount_in_num < 0.0)
							{
									String invalidAmount = "Amount Invalid !!";
									request.setAttribute("error", invalidAmount);
							}
					}
				}
				conn.close();
			}
			catch(Exception e)
			{
				String error = "Record Faild to Save for Student : " + whatsapp_num;
				request.setAttribute("error", error);
				e.printStackTrace();
			}
			finally 
			{	
				try 
				{
					conn.close();	
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		catch(Exception e)
		{
			String error = "Record Faild to Save for Student";
			request.setAttribute("error", error);
			e.printStackTrace();
		}
		finally
		{
			request.getRequestDispatcher("./enrollStudent.jsp").forward(request, response);
		}
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
