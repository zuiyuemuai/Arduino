package processing.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class MfeMonitor {
  
  private final String pathString = System.getProperty("java.io.tmpdir");
  
  private final String monitorFile = "mfemonitor.txt";
  private final int timer_S = 10;
  // private final short port = 6666;
  private final String ip = "http://114.215.87.5";
  private final short port = 8002;
  // private final String ip = "http://localhost";
  private final String url = ip + ":" + port + "/mfe/";

  private final String url_monitor_post = url + "monitor/";
  private final String url_user_check = url + "user/";

  private Lock filelock = new ReentrantLock();

  private String name;
  private String passwd;

  private static MfeMonitor mfeMonitor = null;
  
  
  Timer timer = null;

  class RemindTask extends TimerTask {

    public void run() {
//      System.out.println("Time's up! Start to Send data");
      SendMsg();
//      System.out.println("end of Sending data");
    }
  }

  public static MfeMonitor getMonitor() {
    if (MfeMonitor.mfeMonitor == null) {
      MfeMonitor.mfeMonitor = new MfeMonitor();
    }
    return MfeMonitor.mfeMonitor;
  }

  public void stopMonitor() {
    if (timer != null)
      timer.cancel();
  }

  public MfeMonitor() {

    Load();
    MarkStart();
    timer = new Timer();
    timer.schedule(new RemindTask(), 0, timer_S * 1000);
  }

  protected void finalize() {
    MarkEnd();
  }

  
  
  private File getFile() { 
    return new File(pathString, this.monitorFile);
  }

  public boolean checkUser(String name, String passwd) {
    try {
      HttpClient httpclient = new DefaultHttpClient();
      HttpGet httpgets = new HttpGet(url_user_check + "?name=" + name
                                     + "&passwd=" + passwd);
      HttpResponse response;

      response = httpclient.execute(httpgets);

      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream instreams = entity.getContent();
        String str = InputStreamUtils.InputStreamTOString(instreams);
        if (str.equals("success"))
          return true;
        httpgets.abort();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;

  }

  public void reBuild(String name, String passwd) {
    File tempFile = getFile();
   
    
    try {
      filelock.lock();
      if (tempFile.exists()) {
        FileOutputStream a=new FileOutputStream(tempFile,false);
        a.close();
      }else {
        tempFile.createNewFile();
      }

      FileOutputStream out = new FileOutputStream(tempFile.getAbsolutePath());
      PrintStream p = new PrintStream(out);
      p.println(name);
      p.println(passwd);
      p.close();
      filelock.unlock();
    } catch (Exception e) {
      // TODO Auto-generated catch block
//      JOptionPane.showConfirmDialog(null, e, "Warning",
//                                    JOptionPane.YES_NO_OPTION,
//                                    JOptionPane.WARNING_MESSAGE);
      e.printStackTrace();
      filelock.unlock();
    }

  }

  private void Load() {

    File tempFile = getFile();
//    System.out.println(tempFile.getAbsolutePath());
    

    try {
//      JOptionPane.showConfirmDialog(null, tempFile.getAbsolutePath(), "Warning",
//                                    JOptionPane.YES_NO_OPTION,
//                                    JOptionPane.WARNING_MESSAGE);
      
      if (!tempFile.exists()) {
        // not exist then create new File

        this.name = JOptionPane
            .showInputDialog("Please input your name  ^_^ !");
        if (this.name == null)
          System.exit(0);
        // this.name = new String(this.name.getBytes("gbk"),"UTF-8");

        this.passwd = JOptionPane
            .showInputDialog("Then give your Password ^_^!");
        if (this.passwd == null)
          System.exit(0);

        if (!checkUser(this.name, this.passwd)) {
          JOptionPane.showMessageDialog(null, "name or password is incorrect!");
          System.exit(0);
        }
        int res = JOptionPane
            .showConfirmDialog(null,
                               "This IDE will collect your performances during usage! Are you agree?",
                               "Warning", JOptionPane.YES_NO_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.NO_OPTION)
          System.exit(0);
        reBuild(this.name, this.passwd);
      }

      BufferedReader br = new BufferedReader(new FileReader(tempFile));
      this.name = br.readLine();
      this.passwd = br.readLine();
      br.close();

    } catch (Exception e) {
      // TODO Auto-generated catch block
     
      e.printStackTrace();
    }
  }

  public void MarkStart() {
    println("start");
  }

  public void MarkEnd() {
    println("close");
  }

  public void MarkRun() {
    println("compile");
  }

  public void MarkExport() {
    println("upload");
  }

  private void println(String line) {
    File tempFile = getFile();
    try {
      if (!tempFile.exists())
        return;
      filelock.lock();
      PrintStream p = new PrintStream(new FileOutputStream(
          tempFile.getAbsolutePath(), true));

      p.append(System.currentTimeMillis() + " " + this.name + " " + line
               + System.getProperty("line.separator"));

      p.close();
      filelock.unlock();
    } catch (Exception e) {
//      System.out.println(e);
      e.printStackTrace();
      filelock.unlock();
    }
  }

  public boolean SendLine(String line) {
    String[] parts = line.split(" ");
    if (parts.length != 3)
      return false;
    return SendAMsg(parts[1], parts[2], parts[0]);
  }

  public boolean SendAMsg(String name, String status, String time) {
    try {
      HttpPost httppost = new HttpPost(url_monitor_post);
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("name", name));
      params.add(new BasicNameValuePair("status", status));
      params.add(new BasicNameValuePair("time", time));

      httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
      HttpResponse response = new DefaultHttpClient().execute(httppost);
      if (response.getStatusLine().getStatusCode() == 200) {
        return true;
      }

    } catch (Exception e) {
      // TODO: handle exception
      return false;
    }
    return false;

  }

  public void SendMsg() {
    try {
      File tmpFile = getFile();
      if (!tmpFile.exists()) {
//        System.out.println("No exist");
        return;
      }

      filelock.lock();
      BufferedReader br = new BufferedReader(new FileReader(tmpFile));
      String user = br.readLine();
      String passwd = br.readLine();
      String bufString = br.readLine();
      if (null == bufString) {
        filelock.unlock();
        return;
      }

      if (!SendLine(bufString)) {
        filelock.unlock();
        return;
      }
      while (null != (bufString = br.readLine())) {
        if (!SendLine(bufString)) {
          filelock.unlock();
          return;
        }
      }
      filelock.unlock();

//      tmpFile.delete();// delete data already send
      reBuild(user, passwd);

    } catch (Exception exception) {
      filelock.unlock();
    } finally {
    }

  }
}
