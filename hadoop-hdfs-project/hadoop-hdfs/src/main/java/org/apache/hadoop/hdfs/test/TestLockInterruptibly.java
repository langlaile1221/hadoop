package org.apache.hadoop.hdfs.test;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test lock and LockInterupptibly
 * 而使用LockInterupptibly，则会响应中断
 */
public class TestLockInterruptibly {

  // @Test
  public void test() throws Exception {
    final Lock lock = new ReentrantLock();
    lock.lock();

    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        lock.lock();
        System.out.println(Thread.currentThread().getName() + " interrupted.");
      }
    }, "child thread -1");

    t1.start();
    Thread.sleep(1000);

    t1.interrupt();

    Thread.sleep(1000000);
  }

  // @Test
  public void test3() throws Exception {
    final Lock lock = new ReentrantLock();
    lock.lock();

    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(2000);
          lock.lockInterruptibly();
        } catch (InterruptedException e) {
          System.out.println(Thread.currentThread().getName() + " interrupted.");
        }
      }
    }, "child thread -1");

    t1.start();
    Thread.sleep(1000);
    t1.interrupt();
    Thread.sleep(1000000);
  }

  public static void main(String[] args) throws Exception {
    new TestLockInterruptibly().test();
  }

}

// try{
//     Thread.sleep(2000);
//     lock.lockInterruptibly();
//     }catch(InterruptedException e){
//     System.out.println(Thread.currentThread().getName()+" interrupted.");
//     }
//     t1.start();
//     t1.interrupt();
//     Thread.sleep(1000000);
//     }
// }