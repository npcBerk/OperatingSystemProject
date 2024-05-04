// PartySimulation.java

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;
class PartySimulation {
    private static final int NUM_GUESTS = 8;
    private static final int BOREK_CAPACITY = 30;
    private static final int CAKE_CAPACITY = 15;
    private static final int DRINK_CAPACITY = 30;
    private static final int TRAY_CAPACITY = 5;

    private boolean[] guestFinished = new boolean[NUM_GUESTS];

    private AtomicInteger borekCount = new AtomicInteger(BOREK_CAPACITY);
    private AtomicInteger cakeCount = new AtomicInteger(CAKE_CAPACITY);
    private AtomicInteger drinkCount = new AtomicInteger(DRINK_CAPACITY);

    public static void main(String[] args) {
        PartySimulation party = new PartySimulation();
        party.startParty();
    }

    private void startParty() {
        Thread waiterThread = new Thread(new Waiter(this));
        waiterThread.start();

        for (int i = 0; i < NUM_GUESTS; i++) {
            Thread guestThread = new Thread(new Guest(this, i + 1));
            guestThread.start();
        }
    }

    synchronized void serveBorek(int guestId) {
        if (getBorekCount() > 0) {
            System.out.println("Guest " + guestId + " eats a börek.");
            decrementBorekCount();
            if (getBorekCount() <= 1) {
                refillTray("börek");
            }
        }
    }

    synchronized void serveCake(int guestId) {
        if (getCakeCount() > 0) {
            System.out.println("Guest " + guestId + " eats a slice of cake.");
            decrementCakeCount();
            if (getCakeCount() <= 1) {
                refillTray("cake");
            }
        }
    }

    synchronized void serveDrink(int guestId) {
        if (getDrinkCount() > 0) {
            System.out.println("Guest " + guestId + " drinks a glass.");
            decrementDrinkCount();
            if (getDrinkCount() <= 1) {
                refillTray("drink");
            }
        }
    }

    public void refillTray(String type) {
        System.out.println("Waiter refills " + type + " tray.");
        switch (type) {
            case "börek":

                incrementBorekCount(TRAY_CAPACITY - 1);
                break;
            case "cake":
                incrementCakeCount(TRAY_CAPACITY - 1);
                break;
            case "drink":
                incrementDrinkCount(TRAY_CAPACITY - 1);
                break;
        }
    }
    public synchronized boolean isAllFoodAndDrinkConsumed() {
        return getBorekCount() == 0 && getCakeCount() == 0 && getDrinkCount() == 0;
    }
    public synchronized boolean isAllGuestsFinished() {
        for (int i = 1; i <= NUM_GUESTS; i++) {
            if (guestFinished[i - 1]) {
                continue;
            }
            if (getBorekCount() > 0 || getCakeCount() > 0 || getDrinkCount() > 0) {
                return false;
            }
        }
        return true;
    }

    public int getBorekCount() {
        return borekCount.get();
    }

    public void decrementBorekCount() {
        borekCount.decrementAndGet();
    }

    public void incrementBorekCount(int amount) {
        borekCount.addAndGet(amount);
    }

    public int getCakeCount() {
        return cakeCount.get();
    }

    public void decrementCakeCount() {
        cakeCount.decrementAndGet();
    }

    public void incrementCakeCount(int amount) {
        cakeCount.addAndGet(amount);
    }

    public int getDrinkCount() {
        return drinkCount.get();
    }

    public void decrementDrinkCount() {
        drinkCount.decrementAndGet();
    }

    public void incrementDrinkCount(int amount) {
        drinkCount.addAndGet(amount);
    }
}

class Guest implements Runnable {
    private PartySimulation party;
    private int guestId;
    private boolean finished;

    Guest(PartySimulation party, int guestId) {
        this.party = party;
        this.guestId = guestId;
    }

    @Override
    public void run() {
        while (true) {
            if (party.getBorekCount() > 0 || party.getCakeCount() > 0 || party.getDrinkCount() > 0) {
                int randomChoice = ThreadLocalRandom.current().nextInt(3);
                switch (randomChoice) {
                    case 0:
                        party.serveBorek(guestId);
                        break;
                    case 1:
                        party.serveCake(guestId);
                        break;
                    case 2:
                        party.serveDrink(guestId);
                        break;
                }
            } else {
                break;
            }

            // Limit the number of boreks, cakes, and drinks per guest
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 150)); // Random sleep time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Check if all food and drink are consumed
            if (party.isAllFoodAndDrinkConsumed()) {
                finished = true;
                break;
            }
        }
        System.out.println("Guest " + guestId + " leaves the party.");
    }
}

class Waiter implements Runnable {
    private PartySimulation party;

    Waiter(PartySimulation party) {
        this.party = party;
    }

    @Override
    public void run() {
        while (true) {
            // Wait for a tray to be empty or almost empty
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (party.getBorekCount() <= 1 || party.getCakeCount() <= 1 || party.getDrinkCount() <= 1) {
                party.refillTray("börek");
                party.refillTray("cake");
                party.refillTray("drink");
            }

            // Check if all food and drink are consumed
            if (party.isAllFoodAndDrinkConsumed()) {
                break;
            }

            // Wait for all guests to finish eating and drinking
            while (!party.isAllGuestsFinished()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Check if all guests have finished
            if (party.isAllGuestsFinished()) {
                break;
            }
        }

        System.out.println("Waiter finishes the party.");
    }
}
