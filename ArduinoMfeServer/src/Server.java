import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class Server {

	public static void main(String args[]) throws Exception {
		 MfeMonitor.getMonitor();
		 while(true);
//		System.out.println(System.getProperty("java.class.path"));
//		 HttpClient httpclient = new DefaultHttpClient();
//		 HttpGet httpgets = new HttpGet("http://127.0.0.1:8000/mfe/user?name=s&passwd=223");
//		 HttpResponse response = httpclient.execute(httpgets);
//		 HttpEntity entity = response.getEntity();
//		 if (entity != null) {
//		 InputStream instreams = entity.getContent();
//		 String str = InputStreamUtils.InputStreamTOString(instreams);
//		 System.out.println("Do something");
//		 System.out.println(str);
//		 // Do not need the rest
//		 httpgets.abort();
//		 }

//		// TODO Auto-generated method stub
//		String url = "http://127.0.0.1:8000/mfe/monitor/";
//		// POST��URL
//		HttpPost httppost = new HttpPost(url);
//		// ����HttpPost����
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		// ����һ��NameValuePair���飬���ڴ洢���͵Ĳ���
//		params.add(new BasicNameValuePair("pwd", "2544"));
//		// ��Ӳ���
//		httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//		// ���ñ���
//		HttpResponse response = new DefaultHttpClient().execute(httppost);
//		// ����Post,������һ��HttpResponse����
//		if (response.getStatusLine().getStatusCode() == 200) {// ���״̬��Ϊ200,�������
//			String result = EntityUtils.toString(response.getEntity());
//			// �õ����ص��ַ�
//			System.out.println(result);
//			// ��ӡ���
//		}
	}
}
