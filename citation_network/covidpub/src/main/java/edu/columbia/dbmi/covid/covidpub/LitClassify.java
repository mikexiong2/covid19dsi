package edu.columbia.dbmi.covid.covidpub;

import java.util.HashMap;
import java.util.Map;

public class LitClassify {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String,String> pmid=new HashMap<String,String>();
		String metainfo=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/0423_metadata.txt");
		String[] mrows=metainfo.split("\n");
		for(String mr:mrows){
			System.out.println(mr);
			String[] en=mr.split("\t");
			if(en.length>2){
				System.out.println(en[1]);
				if(en[1].equals("None")==false){
					pmid.put(en[1], mr);
				}
			}
		}
		
		String content=FileUtil.readFile("/Users/cy2465/Downloads/pubmed_ref_project/covid-19_diagnosis.txt");
		String[] rows=content.split("\n");
		for(String r:rows){
			System.out.println("->"+r);
			System.out.println(pmid.get(r));
		}
		
	}

}
