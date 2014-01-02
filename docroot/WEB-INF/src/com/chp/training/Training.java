package com.chp.training;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

/**
 * Portlet implementation class NewPortlet2
 */
public class Training extends MVCPortlet {

	private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Logger logger = Logger.getLogger("InfoLogging");
	
	private static JSONObject requestToJSONObject(PortletRequest request) {
		JSONObject result = new JSONObject();
		Enumeration<String> parametersE = request.getParameterNames();
		while (parametersE.hasMoreElements()) {
			String parameter = parametersE.nextElement();
			String value = request.getParameter(parameter);
			result.put(parameter, value);
		}
		
		return result;
	}
	
	private static void writeMessage(ResourceResponse response, JSONObject jsonObject) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonObject.toJSONString());	
	}

	private static void writeMessage(ResourceResponse response, JSONArray jsonArray) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonArray.toJSONString());
	}

	private static void writeMessage(ResourceResponse response, String string) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/plain;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, string);	
	}
	
	
	// On-the-job training functions
	
	public void search(ResourceRequest ResourceRequest,
			ResourceResponse ResourceResponse)
			throws IOException, PortletException {
	
		System.out.println("searching...");
	}
	
	
	public void getTopCategories(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getTopCategories(con);
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
		
		/*
		JSONArray list = new JSONArray();
        JSONObject cat1 = new JSONObject();
        JSONObject cat2 = new JSONObject();
        JSONObject cat3 = new JSONObject();
        JSONObject cat4 = new JSONObject();
        JSONObject cat5 = new JSONObject();
        cat1.put("id", 10);
        cat1.put("name", "Children");
        cat2.put("id", 20);
        cat2.put("name", "Pregnant women");
        cat3.put("id", 30);
        cat3.put("name", "Elderly");
        cat4.put("id", 40);
        cat4.put("name", "Infections");
        cat5.put("id", 50);
        cat5.put("name", "Mental health");
        list.add(cat1);
        list.add(cat2);
        list.add(cat3);
        list.add(cat4);
        list.add(cat5);
		
		 HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
         httpResponse.setContentType("application/json;charset=UTF-8");
         ServletResponseUtil.write(httpResponse, list.toJSONString());
		*/
	}
	
	
	public void getSubCategories(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		String index = request.getParameter("index");
		JSONObject responseJSON = new JSONObject();
		JSONObject params = requestToJSONObject(request);
		System.out.println(params.toJSONString());
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getNextCategories(con, params);
			responseJSON.put("index",index);
			System.out.println(responseJSON.toJSONString());
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
		
		
		/*
		String catId = request.getParameter("category_id");
		
		// IMPORTANT do not delete
		String index = request.getParameter("index");
		
		JSONArray list = new JSONArray();
        JSONObject cat1 = new JSONObject();
        JSONObject cat2 = new JSONObject();
        JSONObject cat3 = new JSONObject();
        JSONObject cat4 = new JSONObject();
        JSONObject cat5 = new JSONObject();
        cat1.put("id", 10);
        cat1.put("name", "Clinical disorders");
        cat2.put("id", 20);
        cat2.put("name", "Relationships");
        cat3.put("id", 30);
        cat3.put("name", "Medical and Developmental Disorders");
        cat4.put("id", 40);
        cat4.put("name", "Psychosocial Stressors")
        cat5.put("id", 50);
        cat5.put("name", "Emotional and Social Functioning");
        list.add(cat1);
        list.add(cat2);
        list.add(cat3);
        list.add(cat4);
        list.add(cat5);
		
        JSONObject responseJSON = new JSONObject();
        responseJSON.put("index", index);
        responseJSON.put("objects", list);
        
        HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
		*/
	}
	
	
	public void getMaterialTitles(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		// IMPORTANT do not delete
		String index = request.getParameter("index");
		
		JSONArray list = new JSONArray();
		JSONObject mat1 = new JSONObject();
		JSONObject mat2 = new JSONObject();
		JSONObject mat3 = new JSONObject();
		mat1.put("id", "11");
		mat1.put("title", "Post-Traumatic Stress Disorder");
		mat2.put("id", "22");
		mat2.put("title", "Adjustment Disorder");
		mat3.put("id", "33");
		mat3.put("title", "Sleeping Behaviour Disorder");
		list.add(mat1);
		list.add(mat2);
		list.add(mat3);
		
		JSONObject responseJSON = new JSONObject();
        responseJSON.put("index", index);
        responseJSON.put("objects", list);
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	}
	
	
	public void getMaterialContent(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject contentObj = new JSONObject();
		contentObj.put("id", "33");
		contentObj.put("title", "Sleeping behaviour disorder");
		contentObj.put("text", 
				"A sleep disorder, or somnipathy, is a medical disorder of the sleep patterns of a person or animal." +
				" Some sleep disorders are serious enough to interfere with normal physical, mental and emotional " +
				"functioning. Polysomnography is a test commonly ordered for some sleep disorders. Disruptions in " +
				"sleep can be caused by a variety of issues, from teeth grinding (bruxism) to night terrors. When a" +
				" person suffers from difficulty falling asleep and staying asleep with no obvious cause, it is referred " +
				"to as insomnia.[1] Dyssomnia refers to a group of sleep disorders with the symptoms of trouble falling " +
				"asleep or maintaining sleep, which may cause an elevated sense of sleepiness during the day. Insomnia is " +
				"characterized by an extended period of symptoms including trouble with retaining sleep, fatigue," +
				" decreased attentiveness, and dysphoria. To diagnose insomnia, these symptoms must persist for a minimum " +
				"of 4 weeks. The DSM-IV categorizes insomnias into primary insomnia, insomnia associated with medical " +
				"or mental diseases, and insomnia associated with the consumption or abuse of substances. Individuals with" +
				" insomnia often worry about the negative health consequences, which can lead to the development of anxiety" +
				" and depression.[2] In addition, sleep disorders may also cause sufferers to sleep excessively, " +
				"a condition known as hypersomnia. Management of sleep disturbances that are secondary to mental," +
				" medical, or substance abuse disorders should focus on the underlying conditions.");
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, contentObj.toJSONString());
	}
	
	
	public void getTopQuestions(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		/*
		JSONArray list = new JSONArray();
		JSONObject q1 = new JSONObject();
		JSONObject q2 = new JSONObject();
		JSONObject q3 = new JSONObject();
		q1.put("id", "11");
		q1.put("question", "Classify cough or difficult breathing");
		q2.put("id", "22");
		q2.put("question", "Classify diarrhea");
		q3.put("id", "33");
		q3.put("question", "Classify fever");
		list.add(q1);
		list.add(q2);
		list.add(q3);
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
        httpResponse.setContentType("application/json;charset=UTF-8");
        ServletResponseUtil.write(httpResponse, list.toJSONString());
        */
		
		JSONArray list = new JSONArray();
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getTopics(con);
			writeMessage(response,list);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}
	
	
	public void getSubQuestions(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		JSONObject responseJSON = new JSONObject();
		JSONObject params = requestToJSONObject(request);
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			// TODO Peter has changed this function, needs to be updated
			//responseJSON = DataBaseFunctions.getNextQuestionBox(con, requestToJSONObject(request));
			
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}

		/*
		if (request.getParameter("question_id").equals("2")) {
			
			responseJSON.put("title", "Cough or difficult breathing classified");
			responseJSON.put("next", "3");
			JSONObject q1 = new JSONObject();
			JSONObject q2 = new JSONObject();
			JSONObject q3 = new JSONObject();
			q1.put("id", "11");
			q1.put("question", "Are there any general danger signs present?");
			q1.put("description", "Child is unable to drink or breastfeed;child vomits everything; child has had or is having convulsions; " +
					"child is lethargic or unconscious.");
			q2.put("id", "22");
			q2.put("question", "Is there chest indrawing?");
			q2.put("description", "If present, give a trial of rapid acting inhaled bronchodilator for up to three times 15-20 minutes apart. " +
					"Count the breaths again and look for chest indrawing again, then classify.");
			q3.put("id", "33");
			q3.put("question", "Is there stridor in a calm child?");
			list.add(q1);
			list.add(q2);
			list.add(q3);
		}
		else if (request.getParameter("question_id").equals("3")) {
			getTreatment(request, response);
			return;
		}
		else {
			responseJSON.put("title", "Classify cough or difficult breathing");
			responseJSON.put("next", "2");
			JSONObject q1 = new JSONObject();
			JSONObject q2 = new JSONObject();
			q1.put("id", "11");
			q1.put("question", "Does the child have fast breathing?");
			q1.put("description", "If the child is 2-12 months old and they breathe 50 breaths per minute or more; " +
					"if the child 1-5 years old and they breathe 40 breaths per minute or more <p/>");
			q2.put("id", "22");
			q2.put("question", "Does the child have a cough?");
			list.add(q1);
			list.add(q2);
		}
		
		responseJSON.put("questions", list);
			
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/json;charset=UTF-8");
	    ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	    */
	}
	
	
	public void getTreatment(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();
		responseJSON.put("title","Severe pneumonia or very severe disease");
		responseJSON.put("treatment", "Give first dose of an appropriate antibiotic; Refer URGENTLY to hospital.");
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/json;charset=UTF-8");
	    ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	
	}
	
	
	public void sendForm(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		String generalName = ParamUtil.getString(request, "general");
		String generalSym = ParamUtil.getString(request, "generalSymptom_1");
		String cat = ParamUtil.getString(request, "topCategories");
		
		JSONObject params = requestToJSONObject(request);
		System.out.println(params.toJSONString());
		
		// Getting a file
		String folder = getInitParameter("uploadFolder");
		String realPath = getPortletContext().getRealPath("/");
		UploadPortletRequest upr = PortalUtil.getUploadPortletRequest(request);
		File file = upr.getFile("picture_1");
		if (file != null) {
			String filename = upr.getFileName("picture_1");
			File newFile = null;
			newFile = new File(realPath + folder + filename);
			System.out.println(realPath + folder + filename);
			newFile.createNewFile();
			InputStream in = new BufferedInputStream(upr.getFileAsStream("picture_1"));
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(newFile);
			
			byte[] bytes_ = FileUtil.getBytes(in);
			int i = fis.read(bytes_);
			
			while (i != -1) {
				fos.write(bytes_, 0, i);
				i = fis.read(bytes_);
			}
			fis.close();
			fos.close();
			
			System.out.println("sendForm...." + generalName + " " + generalSym + " " + cat + " "
					+ filename);
		}
	}
		
	// Very necessary function, please don't delete anything in here
	@Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
        throws IOException, PortletException {
		
		System.out.println("processAction reached");
		
        PortletPreferences prefs = actionRequest.getPreferences();
        String actionName = actionRequest.getParameter("actionName");
        boolean switchJSP = false;
        
        if (actionName != null) {
        	
        	
        	if (actionName.equals("subCategories")) {
        		
        		String catId1 = actionRequest.getParameter("category_id");
                if (catId1 != null) {
                    actionRequest.setAttribute("category_id", catId1);
                }
        	}
        	else if (actionName.equals("materials")) {
        		System.out.println("materials");
        		String catId2 = actionRequest.getParameter("id");
                if (catId2 != null) {
                    actionRequest.setAttribute("mat_id", catId2);
                }
        	}
        	else if (actionName.equals("goToMaterial")) {
        		Map<String,String[]> params = actionRequest.getParameterMap();
        		for (String key : params.keySet()) {
        			System.out.println(key + ": " + params.get(key));
        		}
        	}
        	else if (actionName.equals("subQuestions")) {
        		String questionId = actionRequest.getParameter("question_id");
                if (questionId != null) {
                	actionRequest.setAttribute("question_id", questionId);
                }
                
                String title = actionRequest.getParameter("title");
                if (title != null) {
                	actionRequest.setAttribute("title", title);
                }
                
                // hackery
                String next = actionRequest.getParameter("question_id");
                if (next != null && next.equals("3")) {
                	System.out.println("switching...");
                	switchJSP = true;
                }
        	}
        }
        
        String jspPage = actionRequest.getParameter("jspPage");
        System.out.println("jspPage: " + jspPage);
        if (jspPage != null) {
        	System.out.println(jspPage);
        	actionResponse.setRenderParameter("jspPage", jspPage);
        }
        if (switchJSP) {
        	actionResponse.setRenderParameter("jspPage", "/html/newportlet2/diagnose.jsp");
        }
        
        super.processAction(actionRequest, actionResponse);
    }
	
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {        
				
		String resourceID = request.getResourceID();
		 
		 if ("getTopCategories".equals(resourceID)) {
			 getTopCategories(request, response);
		 }
		 
		 if ("getSubCategories".equals(resourceID)) {
			 getSubCategories(request, response);
		 }
		 
		 if ("getMaterialTitles".equals(resourceID)) {
			 getMaterialTitles(request, response);
		 }
		 
		 if ("getMaterialContent".equals(resourceID)) {
			 getMaterialContent(request, response);
		 }
		 
		 if ("getTopQuestions".equals(resourceID)) {
			 getTopQuestions(request, response);
		 }
		 
		 if ("getSubQuestions".equals(resourceID)) {
			 getSubQuestions(request, response);
		 }
		 
		 if ("getTreatment".equals(resourceID)) {
			 getTreatment(request, response);		 }
		 
		 if ("sendForm".equals(resourceID)) {
			 //sendForm(request, response);
		 }
	}

}

