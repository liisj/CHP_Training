package com.chp.training;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.types.Types;
import org.postgresql.core.types.PGType;

class DatabaseStatements {
	static final String INSERT_QUESTION_BOX = "INSERT INTO question_boxes "
			+ "(id,description,questions) "
			+ "VALUES (default,?,generate_questions(?,?)) " + "RETURNING id";

	static final String INSERT_TOPIC = "INSERT INTO topics "
			+ "(id,title,description,first_box) " + "VALUES (default,?,?,?) "
			+ "RETURNING id";

	static final String INSERT_TREATMENT = "INSERT INTO treatments "
			+ "(id,description) " + "VALUES (default,?) " + "RETURNING id";

	static final String INSERT_PATH = "INSERT INTO paths "
			+ "(id,path,treatment,topic) " + "VALUES (default,(?)::ltree,?,?)";

	static final String INSERT_ACTION = "INSERT INTO actions (questionbox,yes_count,\"action\",\"reference\") VALUES (?,?,?,?)";

	static final String IS_PATH_END = "SELECT t.description "
			+ "FROM paths p,treatments t " + "WHERE t.id = p.treatment "
			+ "AND p.topic = ? " + "AND ('*.'||?)::lquery ~ p.path";

	static final String GET_TOPICS = "SELECT row_to_json(row)::text FROM (SELECT * FROM topics) row";

	static final String GET_NEXT_QUESTION_BOX_NO_PATH = "WITH paras AS (SELECT ? as topic) "
			+ "SELECT row_to_json(row) "
			+ "FROM (SELECT id, q.description,array_to_json(questions) as questions "
			+ "FROM question_boxes q "
			+ "WHERE q.id IN (SELECT DISTINCT (regexp_split_to_array(ltree2text(subpath(p.path,0,1)),'_'))[1]::integer q_index "
			+ "FROM paths p,paras WHERE paras.topic = p.topic)) row";

	static final String GET_NEXT_QUESTION_BOX_PATH = "WITH paras AS (SELECT ? as topic, ? as pa) "
			+ "SELECT row_to_json(row) "
			+ "FROM (SELECT id, q.description,array_to_json(questions) as questions "
			+ "FROM question_boxes q "
			+ "WHERE q.id IN (SELECT DISTINCT (regexp_split_to_array(ltree2text(subpath(p.path,nlevel(paras.pa::ltree)+index(p.path,paras.pa::ltree),1)),'_'))[1]::integer q_index "
			+ "FROM paths p,paras WHERE paras.topic = p.topic AND ('*.'||paras.pa||'.*')::lquery ~ p.path)) row";

	static class Qu implements PGType {
		public String question;
		public String details;
		public Qu(String q,String d) {
			question = q;
			details = d;
		}
	}
	
	public static void main(String[] args) {
		try {
			Connection con = DataBaseFunctions.getWebConnection();
			
			PreparedStatement p = con.prepareStatement("SELECT row_to_json(lastr.*) " +
					"FROM (SELECT (?)::question) lastr)");
			p.setObject(1, new DatabaseStatements.Qu("a","a").toString(),Types.OTHER);
			System.out.println(p.toString());
			ResultSet rs = p.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
