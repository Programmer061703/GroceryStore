import java.util.concurrent.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;


public class GroceryStore{
    private static final int MAX_WAITING_AREA = 40;
    private static final int MAX_PRODUCE_SECTION = 20;
    private static final int MAX_GENERAL_SECTION = 25;
    private static final int MAX_FROZEN_SECTION = 10;
    private static final int MAX_CASHIER_SECTION = 10;

    private static Semaphore waitingRoom, produceSection, generalSection, frozenSection, cashierSection;
    private static Semaphore waitingMutex, produceMutex, generalMutex, frozenMutex, cashierMutex;
    private static int elapsedTime; 
    




    public static void main(String[] args){

        int sleepTime = 20;
        int numCustomers = 100;


        if(args.length == 2){
            sleepTime = Integer.parseInt(args[0]);
            sleepTime = sleepTime * 1000; 
            numCustomers = Integer.parseInt(args[1]);
            System.out.println("Sleep time: " + sleepTime);
            System.out.println("Number of customers: " + numCustomers);
            System.out.println("");


        }

        else{
            System.out.println("Usage: java GroceryStore <sleep time> <number of customers>");
            System.out.println("Using default values");
            System.out.println("Sleep time: " + sleepTime);
            System.out.println("Number of customers: " + numCustomers);

        }

        waitingRoom = new Semaphore(MAX_WAITING_AREA);
        produceSection = new Semaphore(MAX_PRODUCE_SECTION);
        generalSection = new Semaphore(MAX_GENERAL_SECTION);
        frozenSection = new Semaphore(MAX_FROZEN_SECTION);
        cashierSection = new Semaphore(MAX_CASHIER_SECTION);
        waitingMutex = new Semaphore(1);
        produceMutex = new Semaphore(1);
        generalMutex = new Semaphore(1);
        frozenMutex = new Semaphore(1);
        cashierMutex = new Semaphore(1);

        long startTime = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<Thread>(); 

        for(int i = 0; i< numCustomers; i++){
           elapsedTime = (int)(System.currentTimeMillis() - startTime); 

           if(elapsedTime < sleepTime){
            
            Customer customer = new Customer(i, waitingRoom, produceSection, generalSection, frozenSection, cashierSection, waitingMutex, produceMutex, generalMutex, frozenMutex, cashierMutex);
            Thread thread = new Thread(customer);
            threads.add(thread);
            customer.setThread(thread);
            thread.start();
           

           }
              else{
                    System.out.println("Time is up");
                break;
              }
        }

        for(Thread thread: threads){
            try{
                thread.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        System.out.println("All customers have left the store");

        //Calculate time

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total time: " + totalTime + " milliseconds");


    }

}

 class Customer implements Runnable{
    private int id; 
    

    private  Semaphore Swait;
    private  Semaphore Sproduce;
    private  Semaphore Sgeneral;
    private  Semaphore Sfrozen;
    private  Semaphore Scashier;
    private  Semaphore Mwait = new Semaphore(1);
    private  Semaphore Mproduce = new Semaphore(1);
    private  Semaphore Mgeneral = new Semaphore(1);
    private  Semaphore Mfrozen = new Semaphore(1);
    private  Semaphore Mcashier = new Semaphore(1);
    private Thread thread;
    private final Random generator = new Random();

    private static int numInWait = 0; 
    private static int numInProduce = 0;
    private static int numeInGeneral = 0;
    private static int numInFrozen = 0;
    private static int numInCash = 0;

        Customer(int id, Semaphore Swait, Semaphore Sproduce,
                    Semaphore Sgeneral, Semaphore Sfrozen, Semaphore Scashier, Semaphore Mwait, Semaphore Mproduce, Semaphore Mgeneral, Semaphore Mfrozen, Semaphore Mcashier){

            this.id = id;
            this.Swait = Swait;
            this.Sproduce = Sproduce;
            this.Sgeneral = Sgeneral;
            this.Sfrozen = Sfrozen;
            this.Scashier = Scashier;
            this.Mwait = Mwait;
            this.Mproduce = Mproduce;
            this.Mgeneral = Mgeneral;
            this.Mfrozen = Mfrozen;
            this.Mcashier = Mcashier;


            
            


        }

        public void setThread(Thread thread){
            this.thread = thread;

        }

        private void getWaitingRoom(){

            //Get the waiting room
            try{
                if(Swait.tryAcquire()){
                    Mwait.acquire();
                    numInWait++;
                    System.out.println("Customer" + id + "Entered the store"); 
                    System.out.println("\tCustomer " + id + " is in the waiting area");
                    System.out.println("\tNumber of customers in the waiting area: " + numInWait);
                }
                else{
                    System.out.println("Customer " + id + " is leaving the store because the waiting area is full");
                    thread.interrupt();
                    return;
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            finally{
                Mwait.release();
            }


        }

        private void getProduceSection(){
            try{

                Sproduce.acquire();
                Swait.release(); 

                Mproduce.acquire();
                Mwait.acquire();
                numInProduce++;
                numInWait--;

                System.out.println("\t\tCustomer " + id + " is in the produce section");
                System.out.println("\t\tNumber of customers in the produce section: " + numInProduce);

                Mwait.release();
                Mproduce.release();

                Thread.sleep(generator.nextInt(2000)); 



            }catch(InterruptedException e){
                e.printStackTrace();
            }
            finally{
                Sproduce.release();
            }


        }

        private void getGeneralSection(){
                try{
                    Sgeneral.acquire();
                    Sproduce.release();

                    Mgeneral.acquire();
                    Mproduce.acquire();

                    numeInGeneral++;
                    numInProduce--;

                    System.out.println("\t\t\tCustomer " + id + " is in the general section");
                    System.out.println("\t\t\tNumber of customers in the general section: " + numeInGeneral);

                    Mproduce.release();
                    Mgeneral.release();

                    Thread.sleep(generator.nextInt(2000));
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                finally{
                    Sgeneral.release();
                }


        }

        private void getFrozenSection(){

            try{
                Sfrozen.acquire();
                Sgeneral.release();

                Mfrozen.acquire();
                Mgeneral.acquire();

                numInFrozen++;
                numeInGeneral--;

                System.out.println("\t\t\tCustomer " + id + " is in the frozen section");
                System.out.println("\t\t\tNumber of customers in the frozen section: " + numInFrozen);

                Mgeneral.release();
                Mfrozen.release();

                Thread.sleep(generator.nextInt(2000));

            }catch(InterruptedException e){
                e.printStackTrace();
            }
            finally{
                Sfrozen.release();
            }

        }

        private void getCashierSectio(){

            try{
                Scashier.acquire();
                Sfrozen.release();

                Mcashier.acquire();
                Mfrozen.acquire();

                numInCash++;
                numInFrozen--;

                System.out.println("\t\t\t\tCustomer " + id + " is in the cashier section");
                System.out.println("\t\t\t\tNumber of customers in the cashier section: " + numInCash);

                Mfrozen.release();
                Mcashier.release();

                Thread.sleep(generator.nextInt(2000));

            }catch(InterruptedException e){
                e.printStackTrace();
            }
            finally{
                Scashier.release();
            }

        }

        private void exitGroceryStore(){
            Scashier.acquireUninterruptibly();
            numInCash--;
            Scashier.release();
            Mcashier.release(); 

            System.out.println("Customer " + id + " has left the store");


        }

        public void run(){
           
            try{
                Thread.sleep(new Random().nextInt(10000)); 
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            getWaitingRoom();
            getProduceSection();
            getGeneralSection();
            getFrozenSection();
            getCashierSectio();
            exitGroceryStore();

        }




}