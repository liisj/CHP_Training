package com.chp.training;

class DatabaseStatements {
	static final String INSERT_QUESTION_BOX = "INSERT INTO question_boxes " +
			"(id,description,questions) " +
			"VALUES (default,?,(?)::question[]) " +
			"RETURNING id";

	static final String INSERT_TOPIC = "INSERT INTO topics " +
			"(id,title,description) " +
			"VALUES (default,?,?) " +
			"RETURNING id";

	static final String INSERT_TREATMENT = "INSERT INTO treatments " +
			"(id,description) " +
			"VALUES (default,?) " +
			"RETURNING id";

	static final String INSERT_PATH = "INSERT INTO paths " +
			"(id,path,treatment,topic) " +
			"VALUES (default,(?)::ltree,?,?)";

	static final String IS_PATH_END= "SELECT t.description " +
			"FROM paths p,treatments t " +
			"WHERE t.id = p.treatment " +
			"AND p.topic = ? " +
			"AND ('*.'||?)::lquery ~ p.path";
	
	static final String GET_TOPICS = "SELECT row_to_json(row)::text FROM (SELECT * FROM topics) row";

	static final String GET_NEXT_QUESTION_BOX = "WITH paras AS (SELECT ? as topic, ?::text as pa) " +
			"SELECT row_to_json(row) " +
			"FROM (SELECT id, q.description,array_to_json(questions) as questions " +
			"FROM question_boxes q " +
			"WHERE q.id IN (SELECT DISTINCT (regexp_split_to_array(ltree2text(subpath(p.path,nlevel(paras.pa::ltree)+index(p.path,paras.pa::ltree),1)),'_'))[1]::integer q_index " +
			"FROM paths p,paras WHERE paras.topic = p.topic AND ('*.'||paras.pa||'.*')::lquery ~ p.path)) row";

}
