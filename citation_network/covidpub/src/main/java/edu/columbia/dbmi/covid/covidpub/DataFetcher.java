package edu.columbia.dbmi.covid.covidpub;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DataFetcher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String date="0423";
		String filepath="/Users/cy2465/Downloads/pubmed_ref_project/"+date+"pmc_result.xml";
		List<Article> articles=dataprocessFromPMCXML(filepath);
		generateJSONfile(date, articles);
	}
	
	public static String generateJSONfile(String date,List<Article> artiles){
		
		JSONArray nodes=new JSONArray();
		Set<String> covid19pmids=new HashSet<String>();
		Set<String> covid19title=new HashSet<String>();
		Set<String> covid19doi=new HashSet<String>();
		
		for(int i=0;i<artiles.size();i++){
			Article article=artiles.get(i);
			if(article.getPmid().endsWith("None")==false){
				System.out.println("Add to dic:"+article.getPmid()+"\t"+i);
				covid19pmids.add(article.getPmid());
			}
			if(article.getTitle().endsWith("None")==false){
				System.out.println("Add to dic:"+article.getTitle()+"\t"+i);
				covid19title.add(article.getTitle().toLowerCase());
			}
			if(article.getDoi().endsWith("None")==false){
				System.out.println("Add to dic:"+article.getDoi()+"\t"+i);
				covid19doi.add(article.getDoi());
			}
			artiles.get(i).setId(i);
			JSONObject node=new JSONObject();
			node.accumulate("name", article.getPmcid());
			node.accumulate("title", article.getTitle());
			node.accumulate("keywords", "");
			node.accumulate("group", 1);
			nodes.add(node);
		}
		StringBuffer articlesb=new StringBuffer();
		for(Article a:artiles){
			//String outputId=a.getId();
			articlesb.append(a.getId()+"\t"+a.getPmid()+"\t"+a.getPmcid()+"\t"+a.getTitle().replace("\n", "")+"\t"+a.getPubdate()+"\t"+a.getKeywords()+"\n");
		}
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/"+date+"_metadata.txt", articlesb.toString());
		
		JSONArray links=new JSONArray();
		StringBuffer citesb=new StringBuffer();
		Map<String,Integer> citationmap=new HashMap<String,Integer>();
		for(int i=0;i<artiles.size();i++){
			Article article=artiles.get(i);
			List<Ref> reflist=article.getRefs();
			for(Ref ref:reflist){
				//System.out.println("Ref title:"+ref.getTitle());
				if(covid19pmids.contains(ref.getPmid())||covid19doi.contains(ref.getDoi())){
					System.out.println("PMID check:"+covid19pmids.contains(ref.getPmid()));
					System.out.println("Doi check:"+covid19doi.contains(ref.getDoi()));
					System.out.println("LINK:"+artiles.get(i).getPmcid()+"\t"+ref.getPmid());
					if(ref.getPmid().equals("None")==false){
						int targetindex=findArticleId(ref.getPmid(),ref.getDoi(),artiles);
						if(targetindex>=0){
							System.out.println("Add link:"+i+"\t"+targetindex);
							JSONObject jo=new JSONObject();
							jo.accumulate("source", i);
							jo.accumulate("target", targetindex);
							jo.accumulate("value", 1);
							links.add(jo);
							//citesb.append(artiles.get(i).getPmcid()+"\t"+ref.getPmid()+"\n");
							citesb.append(i+"\t"+targetindex+"\n");
							String tstr=String.valueOf(targetindex);
							if(citationmap.containsKey(tstr)){
								Integer refcount=citationmap.get(tstr);
								refcount++;
								citationmap.put(tstr, refcount);
							}else{
								Integer refcount=1;
								citationmap.put(tstr, refcount);
							}
						}
					}
				}
			}
		}
		JSONObject data=new JSONObject();
		data.accumulate("nodes", nodes);
		data.accumulate("links", links);
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/"+date+"data.json", data.toString());	
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/"+date+"_cite_pair.txt", citesb.toString());
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(citationmap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		StringBuffer refcountsb = new StringBuffer();
		for (Map.Entry<String, Integer> mapping : list) {
			//System.out.println(mapping.getKey() + ":" + mapping.getValue());
			refcountsb.append(mapping.getKey() + "\t" + mapping.getValue() + "\n");
		}
		FileUtil.write2File("/Users/cy2465/Downloads/pubmed_ref_project/"+date+"_cite_count.txt", refcountsb.toString());
		return null;
	}
	
	public static int findArticleId(String pmid,String doi,List<Article> list){
		for(Article article:list){
			if(pmid.equals("None")==false && article.getPmid().equals(pmid)){
				return article.getId();
			}
			if(doi.equals("None")==false &&article.getDoi().equals(doi)){
				return article.getId();
			}
		}
		return -1;
	}
	
	
	public static List<Article> dataprocessFromPMCXML(String filepath){
		String XMLstr=FileUtil.readFile(filepath);
		//System.out.println(XMLstr);
		try {
			//List<String> refs=new ArrayList<String>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(XMLstr)));
			NodeList list = doc.getElementsByTagName("article");
//			if(list.getLength()>1){
//				//System.out.println(">>>>>>>>1");
//			}
			System.out.println("count="+list.getLength());
			List<Article> covid19articles=new ArrayList<Article>();
			for(int i=0;i<list.getLength();i++){
				Article covid19art=new Article();
				Element element = (Element) list.item(i);
				//element.getChildNodes();
				covid19art.setId(i);
				NodeList artlist =element.getElementsByTagName("article-meta");
				//System.out.println("_____");
				for(int j=0;j<artlist.getLength();j++){
					Element amelement =(Element) artlist.item(j);
					NodeList idlist =amelement.getElementsByTagName("article-id");
					for(int z=0;z<idlist.getLength();z++){
						Node n=idlist.item(z);
						NamedNodeMap nnmp=n.getAttributes();
						String pub_id_type=nnmp.getNamedItem("pub-id-type").getNodeValue();
						String id=idlist.item(z).getTextContent();
						System.out.println(pub_id_type);
						System.out.println(id);
						if(pub_id_type.equals("pmid")){
							covid19art.setPmid(id);
						}else if(pub_id_type.equals("pmc")){
							covid19art.setPmcid(id);
						}else if(pub_id_type.equals("doi")){
							covid19art.setDoi(id);
						}
					}
				}
				//title-group
				System.out.println("---------title-group-------------");
				
				NodeList titlegroup =element.getElementsByTagName("title-group");
				StringBuffer titlesb=new StringBuffer();
				for(int t=0;t<titlegroup.getLength();t++){
					Element titleelement =(Element) titlegroup.item(t);
					NodeList atitle =titleelement.getElementsByTagName("article-title");
					for(int b=0;b<atitle.getLength();b++){
						String titlestr=atitle.item(b).getTextContent();
						titlesb.append(titlestr);
					}
				}
				covid19art.setTitle(titlesb.toString());
				//StringBuffer pubdate=new StringBuffer();
				System.out.println("---------Pub Date-------------");
				NodeList pubdlist =element.getElementsByTagName("pub-date");
				String date="01";
				String month="01";
				String year="1900";
				
				if(pubdlist.getLength()>0){
					NodeList pubdatainfo = pubdlist.item(0).getChildNodes();
					System.out.println(pubdlist.item(0).getTextContent());
					for(int pi=0;pi<pubdatainfo.getLength();pi++){
						Node n=pubdatainfo.item(pi);
						if(element.getElementsByTagName("day").getLength()>0){
							date=element.getElementsByTagName("day").item(0).getTextContent();
						}
						if(element.getElementsByTagName("month").getLength()>0){
							month=element.getElementsByTagName("month").item(0).getTextContent();
						}
						if(element.getElementsByTagName("year").getLength()>0){
							year=element.getElementsByTagName("year").item(0).getTextContent();
						}
						//pubdate.append(pubdatainfo.item(pi).getTextContent());
						
					}
				}
				//pubdate.append(year+"-"+month+"-"+date);
				covid19art.setPubdate(year+"-"+month+"-"+date);
				
				System.out.println("---------KEY WORDS-------------");
				List<String> keywords=new ArrayList<String>();
				NodeList kwlist =element.getElementsByTagName("kwd");
				for(int k=0;k<kwlist.getLength();k++){
					System.out.println(kwlist.item(k).getTextContent());
					keywords.add(kwlist.item(k).getTextContent());
				}
				covid19art.setKeywords(keywords);
				
				System.out.println("----------Ref Start---------");
				NodeList reflist =element.getElementsByTagName("ref");
				System.out.println("----------Ref count:"+reflist.getLength()+"---------");
				List<Ref> refarticles=new ArrayList<Ref>();
				for(int a=0;a<reflist.getLength();a++){
					Ref ra=new Ref();
					Element refelement =(Element) reflist.item(a);
					NodeList citepubidlist =refelement.getElementsByTagName("pub-id");
					for(int b=0;b<citepubidlist.getLength();b++){
						Node n=citepubidlist.item(b);
						NamedNodeMap nnmp=n.getAttributes();
						String cite_pub_id_type=nnmp.getNamedItem("pub-id-type").getNodeValue();
						String cite_id=citepubidlist.item(b).getTextContent();
						System.out.println(cite_pub_id_type);
						System.out.println(cite_id);
						if(cite_pub_id_type.equals("pmid")){
							ra.setPmid(cite_id);
						}if(cite_pub_id_type.equals("doi")){
							ra.setDoi(cite_id);
						}
					}
					NodeList citetitlelist =refelement.getElementsByTagName("article-title");
					for(int b=0;b<citetitlelist.getLength();b++){
						String cite_title=citetitlelist.item(b).getTextContent();
						ra.setTitle(cite_title);
					}
					refarticles.add(ra);
				}
				covid19art.setRefs(refarticles);
				System.out.println("ref count="+refarticles.size());
				System.out.println("----------Ref End----------");
				covid19articles.add(covid19art);
			}
			return covid19articles;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
