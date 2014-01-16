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
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

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

	private static void writeMessage(PortletResponse response, JSONObject jsonObject) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonObject.toJSONString());	
	}


	private static void writeMessage(PortletResponse response, JSONArray jsonArray) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonArray.toJSONString());
	}


	private static void writeMessage(PortletResponse response, String string) throws IOException {
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
	
	
	public void getCategories(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		System.out.println("getCategoreis");
		
		String index = request.getParameter("index");
		JSONObject responseJSON = new JSONObject();

		JSONObject parameters = requestToJSONObject(request);
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getCategories(con,parameters);
			if (index != null) {
				responseJSON.put("index",index);
			}
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}

	public void addCategory(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();

		JSONObject parameters = requestToJSONObject(request);
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.addCategory(con,parameters);
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}

	public void getMaterial(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();

		JSONObject parameters = requestToJSONObject(request);
		
		String index = ParamUtil.getString(request, "index");
		String categoryId = ParamUtil.getString(request, "category_id");
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getMaterial(con,parameters);
			responseJSON.put("index", index);
			responseJSON.put("category_id", categoryId);
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}

	public void addMaterial(PortletRequest request, PortletResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();

		JSONObject parameters = requestToJSONObject(request);
		System.out.println("addMaterials parameters: " + parameters.toString());
		System.out.println(ParamUtil.getString(request, "materialTitle"));
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.addMaterial(con,parameters);
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}
	
	
	
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public void getFirstQuestionBox(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();
		JSONObject parameters = requestToJSONObject(request);
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getFirstQuestionBox(con, parameters);
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings("unchecked")
	public void getSubQuestions(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {

		JSONObject responseJSON = new JSONObject();
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			responseJSON = DataBaseFunctions.getNextAction(con, requestToJSONObject(request));
			writeMessage(response,responseJSON);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} catch (ParseException e) {
			e.printStackTrace();
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
	
	
	@SuppressWarnings("unchecked")
	public void getTreatment(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject responseJSON = new JSONObject();
		responseJSON.put("title","Severe pneumonia or very severe disease");
		responseJSON.put("treatment", "Give first dose of an appropriate antibiotic; Refer URGENTLY to hospital.");
		
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/json;charset=UTF-8");
	    ServletResponseUtil.write(httpResponse, responseJSON.toJSONString());
	
	}
	
	@SuppressWarnings("unchecked")
	public void sendMaterialsForm(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		// Getting a file and saving it to the "uploads" folder
		
		System.out.println(requestToJSONObject(request).toString());
		
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
			int j = fis.read(bytes_);
			
			while (j != -1) {
				fos.write(bytes_, 0, j);
				j = fis.read(bytes_);
			}
			fis.close();
			fos.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendForm(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		JSONObject params = requestToJSONObject(request);
		System.out.println("original params: " + params.toJSONString());
		
		JSONObject realParams = new JSONObject();
		
		// Add information about general diagnosis
		JSONObject topicObject = new JSONObject();
		topicObject.put("title", ParamUtil.getString(request, "general"));
		
		// Add symptoms to general diagnosis
		JSONObject questionBoxObject = new JSONObject();
		int totalSymptoms = extractSymptoms(request, questionBoxObject, "generalSymptom_", "generalDescr_");
		realParams.put("questionbox", questionBoxObject);
		realParams.put("topic", topicObject);
		
		String yesCount = ParamUtil.getString(request, "generalNr");
		
		// Add sub-diagnoses
		extractDiagnoses(1, request, realParams, yesCount, "sub", totalSymptoms);
		
		System.out.println("realParams: " + realParams.toJSONString());
		
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			DataBaseFunctions.insertNewQuestionPath(con, realParams);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		}
	}

	// Extract symptoms for a questionbox
	@SuppressWarnings("unchecked")
	private int extractSymptoms(ActionRequest request, JSONObject questionBoxObject,
								String symptomBase, String descrBase) {
		
		JSONArray questions = new JSONArray();
		
		int i = 1;
		while (true) {
			JSONObject questionObject = new JSONObject();
			String question = ParamUtil.getString(request, symptomBase + i);
			if (question.equals("")) {
				break;
			}
			
			String description = ParamUtil.getString(request, descrBase + i);
			questionObject.put("question", question);
			questionObject.put("details", description);
			questions.add(questionObject);
			i += 1;
		}
		
		questionBoxObject.put("questions", questions);
		return i-1;
	}
		
	@SuppressWarnings("unchecked")
	private void extractDiagnoses(int i, ActionRequest request, JSONObject superObject, 
			String yesCount, String type, int totalSymptoms) {

		JSONObject subQuestionBoxObject = new JSONObject();
		boolean isSubDiagnosisPresent;
		String subDiagnosis = ParamUtil.getString(request, "diagnosis_" + Integer.toString(i));
		
		if (subDiagnosis.equals("")) {
			isSubDiagnosisPresent = false;
		}
		
		else {
			isSubDiagnosisPresent = true;
			subQuestionBoxObject.put("description", subDiagnosis);
		}
		
		StringBuilder nextKeyLower = new StringBuilder();
		for (int j = 0; j < Integer.parseInt(yesCount); j++) {
			nextKeyLower.append(j + ",");
		}
		
		StringBuilder nextKeyHigher = new StringBuilder();
		for (int j = Integer.parseInt(yesCount); j <= totalSymptoms; j++) {
			nextKeyHigher.append(j + ",");
		}
		
		// If we continue on the same diagnosis level, then not achieving yes_count of symptoms
		// will lead to the next question box on the same level.
		// Achieving yes_count will present the treatment.
		if (type.equals("next")) {
			System.out.println("Type is next");
			JSONObject nextObjectNeg = new JSONObject();
			// possible actions: next_box, treatment, next_topic, change_topic
			if(!isSubDiagnosisPresent) {
				nextObjectNeg.put("action", "next_topic");
			}
			else {
				
				int totalSymptomsNext = extractSymptoms(request, subQuestionBoxObject, "subSymptom_" + i + "_", "subDescr_" + i + "_");
				String yesCountNext = ParamUtil.getString(request, "symptomNr_" + i);
				extractDiagnoses(i+1, request, nextObjectNeg, yesCountNext, "next", totalSymptomsNext);
				
				nextObjectNeg.put("action", "next_box");
				nextObjectNeg.put("questionbox", subQuestionBoxObject);
			}
			
			superObject.put(nextKeyLower, nextObjectNeg);	
			
			JSONObject nextObjectPos = new JSONObject();
			JSONObject treatmentObject = new JSONObject();
			nextObjectPos.put("action", "treatment");
			treatmentObject.put("description", ParamUtil.getString(request, "treatment_" + (i-1)));
			nextObjectPos.put("treatment", treatmentObject);
			superObject.put(nextKeyHigher, nextObjectPos);
		}
		
		// If we go on a more detailed diagnosis level, then not achieving yes_count
		// will lead to the next topic, and achieving yes_count will go to the first
		// more detailed questionbox. 
		// At some point this and the previous should be joined to make a general system of
		// staying on the same level or going one level lower, so that more than 2 levels of
		// detail would be supported.
		if (type.equals("sub")) {
			JSONObject nextObjectNeg = new JSONObject();
			// possible actions: next_box, treatment, next_topic, change_topic
			nextObjectNeg.put("action", "next_topic");
			superObject.put(nextKeyLower, nextObjectNeg);
			
			JSONObject nextObjectPos = new JSONObject();
			
			int totalSymptomsNext = extractSymptoms(request, subQuestionBoxObject, "subSymptom_" + i + "_", "subDescr_" + i + "_");
			String yesCountNext = ParamUtil.getString(request, "symptomNr_" + i);
			extractDiagnoses(i+1, request, nextObjectPos, yesCountNext, "next", totalSymptomsNext);
			
			// possible actions: next_box, treatment, next_topic, change_topic
			nextObjectPos.put("action", "next_box");
			nextObjectPos.put("questionbox", subQuestionBoxObject);
			superObject.put(nextKeyHigher, nextObjectPos);
		}
	}
	
	// Very necessary function, please don't delete anything in here
	@Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
        throws IOException, PortletException {
		
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
        		String catId2 = actionRequest.getParameter("mat_id");
        		System.out.println("catId2: " + catId2);
                if (catId2 != null) {
                    actionRequest.setAttribute("mat_id", catId2);
                }
        	}
        	else if (actionName.equals("goToMaterial")) {
        		// TODO What should be here?
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
        if (jspPage != null) {
        	actionResponse.setRenderParameter("jspPage", jspPage);
        }
        if (switchJSP) {
        	actionResponse.setRenderParameter("jspPage", "/html/newportlet2/diagnose.jsp");
        }
        
        super.processAction(actionRequest, actionResponse);
    }
	
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {        
				
		String resourceID = request.getResourceID();
		 
		 if ("getCategories".equals(resourceID)) {
			 getCategories(request, response);
		 }

		 if ("addCategory".equals(resourceID)) {
			 addCategory(request, response);
		 }

		 if ("addMaterial".equals(resourceID)) {
			 addMaterial(request, response);
		 }

		 if ("getMaterial".equals(resourceID)) {
			 getMaterial(request, response);
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
		 
		 if ("getFirstQuestionBox".equals(resourceID)) {
			 getFirstQuestionBox(request, response);
		 }
		 
		 if ("getSubQuestions".equals(resourceID)) {
			 getSubQuestions(request, response);
		 }
		 
		 if ("getTreatment".equals(resourceID)) {
			 getTreatment(request, response);		 }
		 
	}

}

