package processing.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

public class MfeMonitor {

  private final String monitorFile = "fmemonitor.txt";
  private final int timer_S = 60;
  private final String ip = "114.215.87.5";
  private final short port = 6666;
  private Lock filelock = new ReentrantLock();

  private String name;
  private String passwd;

  private static MfeMonitor mfeMonitor = null;

  Timer timer;

  class RemindTask extends TimerTask {
    public void run() {
      // System.out.println("Time's up! Start to Send data");
      SendMsg();
      // System.out.println("end of Sending data");
      // timer.cancel(); // Terminate the timer thread
    }
  }

  public static MfeMonitor getMonitor() {
    if (MfeMonitor.mfeMonitor == null) {
      MfeMonitor.mfeMonitor = new MfeMonitor();
    }
    return MfeMonitor.mfeMonitor;
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

  private boolean checkUser(String user, String passwd) {
    return true;
  }

  private File getFile() {
    return new File(System.getProperty("java.io.tmpdir"), this.monitorFile);
  }

  public void reBuild(String name, String passwd) {
    File tempFile = getFile();
    try {
      filelock.lock();
      tempFile.createNewFile();

      FileOutputStream out = new FileOutputStream(tempFile.getAbsolutePath());
      PrintStream p = new PrintStream(out);
      p.println(name);
      p.println(passwd);
      p.close();
      filelock.unlock();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      filelock.unlock();
    }

  }

  private void Load() {

    File tempFile = new File(System.getProperty("java.io.tmpdir"),
        this.monitorFile);
    System.out.println(tempFile.getAbsolutePath());
    try {
      if (!tempFile.exists()) {
        // not exist then create new File

        this.name = JOptionPane
            .showInputDialog("Please input your name  ^_^ !");
        this.passwd = JOptionPane
            .showInputDialog("Then give your Password ^_^!");
        int res = JOptionPane
            .showConfirmDialog(null,
                               "This IDE will collect your performance in learning! Are you agree?",
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
      if (!checkUser(this.name, this.passwd)) {
        System.exit(0);
      }

      // then send the data
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void MarkStart() {
    println("start");
  }

  public void MarkEnd() {
    println("end");
  }

  public void MarkRun() {
    println("run");
  }

  public void MarkExport() {
    println("export");
  }

  private void println(String... lines) {
    File tempFile = new File(System.getProperty("java.io.tmpdir"),
        this.monitorFile);
    try {
      if (!tempFile.exists())
        return;
      filelock.lock();
      PrintStream p = new PrintStream(new FileOutputStream(
          tempFile.getAbsolutePath(), true));
      for (String line : lines) {
        p.append(System.currentTimeMillis() + "@" + this.name + ":" + line
                 + System.getProperty("line.separator"));
      }
      p.close();
      filelock.unlock();
    } catch (IOException e) {
      e.printStackTrace();
      filelock.unlock();
    }
  }

  public void SendMsg() {
    Socket socket = null;
    try {
      File tmpFile = getFile();
      if (!tmpFile.exists()) {
        // System.out.println('1');
        return;
      }

      filelock.lock();
      BufferedReader br = new BufferedReader(new FileReader(tmpFile));
      String user = br.readLine();
      String passwd = br.readLine();
      String bufString = br.readLine();
      if (null == bufString) {
        filelock.unlock();
        // System.out.println('2');
        return;
      }

      socket = new Socket(ip, port);
      OutputStream outputStream = socket.getOutputStream();

      PrintStream p = new PrintStream(outputStream);
      p.println(bufString);
      while (null != (bufString = br.readLine())) {
        p.println(bufString);
      }
      p.flush();
      p.close();
      filelock.unlock();

      outputStream.flush();
      tmpFile.delete();// delete data already send
      reBuild(user, passwd);

      outputStream.close();
      socket.close();

    } catch (Exception e) {
//      e.printStackTrace();
      System.out.println("network error");
      filelock.unlock();
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }
}
