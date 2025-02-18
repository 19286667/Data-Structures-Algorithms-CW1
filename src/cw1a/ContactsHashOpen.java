package cw1a;

/**
 *
 * @author DL 2025-01
 */
public class ContactsHashOpen implements IContactDB {  
    private final int initialTableCapacity = 1000;
    private Contact[] table;
    private int tableCapacity;
    private int numEntries;
    private int totalVisited = 0;
    

    private static final double maxLoadFactor = 70.0;
            
    public int getNumEntries(){return numEntries;}
    public void resetTotalVisited() {totalVisited = 0;}
    public int getTotalVisited() {return totalVisited;}

    public ContactsHashOpen() {
        System.out.println("Hash Table with open addressing");
        this.tableCapacity = initialTableCapacity;
        table = new Contact[tableCapacity];
        clearDB();
    }

    /**
     * Empties the database.
     *
     * @pre true
     */
    public void clearDB() {
        for (int i = 0; i != table.length; i++) {
            table[i] = null;
        }
        numEntries = 0;
    }

    private int hash(String s) {
        assert  s != null && !s.trim().equals(""); 
        
        // VERY BAD HASH FUNCTION -- you must improve on this
        
        return Math.abs((s.charAt(0)) % table.length);   
        
    }

    private double loadFactor() {
        return (double) numEntries / (double) table.length * 100.0; 
        // note need for cast to double
    }

    private int findPos(String name) {
        assert name != null && !name.trim().equals("");
        int pos = hash(name);
        int numVisited = 1;  
        System.out.println("finding " + pos + ": " + name );
        while (table[pos] != null && !name.equals(table[pos].getName())) {
           System.out.println("Visiting bucket " + pos + ": " + table[pos] );
           numVisited++;
           pos = (pos + 1) % table.length; // linear probing
        }  
        System.out.println("number of  buckets visited = " + numVisited);
        totalVisited += numVisited;
      
        assert table[pos] == null || name.equals(table[pos].getName());
        return pos;
    }

    /**
     * Determines whether a Contact name exists as a key inside the database
     *
     * @pre name not null or empty string
     * @param name the Contact name (key) to locate
     * @return true iff the name exists as a key in the database
     */
    public boolean containsName(String name) {
        assert name != null && !name.equals("");
        int pos = findPos(name);
        return get(name) != null;
    }

    /**
     * Returns a Contact object mapped to the supplied name.
     *
     * @pre name not null or empty string
     * @param name The Contact name (key) to locate
     * @return the Contact object mapped to the key name if the name exists as
     * key in the database, otherwise null
     */
    @Override
    public Contact get(String name) {
        assert name != null && !name.trim().equals("");
        Contact result;
        int pos = findPos(name);       
        if (table[pos] == null) {
            result = null; // not found
        } else {
            result = table[pos];
        }
        return result;
    }

    /**
     * Returns the number of contacts in the database
     *
     * @pre true
     * @return number of contacts in the database. 0 if empty
     */
    public int size() {return numEntries; }

    /**
     * Determines if the database is empty or not.
     *
     * @pre true
     * @return true iff the database is empty
     */
    @Override
    public boolean isEmpty() {return numEntries == 0; }

    
    private Contact putWithoutResizing(Contact contact) {
      String name = contact.getName();
      int pos = findPos(name);
      Contact previous;
      assert table[pos] == null || name.equals(table[pos].getName());
      previous = table[pos]; // old value
      if (previous == null) { // new entry
         table[pos] = contact;
         numEntries++;
      } else {
         table[pos] = contact; // overwriting 
         
      }
      return previous;
   }
    
    /**
     * Inserts a contact object into the database, with the key of the supplied
     * contact's name. Note: If the name already exists as a key, then then the
     * original entry is overwritten. This method should return the previous
     * associated value if one exists, otherwise null
     *
     * @pre contact not null or empty string
     */
    public Contact put(Contact contact) {
        assert contact != null;
       Contact previous;
        String name = contact.getName();
        assert name != null && !name.trim().equals("");
        previous =  putWithoutResizing(contact);
        if (previous == null && loadFactor() > maxLoadFactor) resizeTable();
        return previous;
    }

    /**
     * Removes and returns a contact from the database, with the key the
     * supplied name.
     *
     * @param name The name (key) to remove.
     * @pre name not null or empty string
     * @return the removed contact object mapped to the name, or null if the
     * name does not exist.
     */
    public Contact remove(String name) {
        assert name != null && !name.trim().equals("");
        int pos = findPos(name);
        System.out.println("remove not yet implemented");
        
        return null;
    }

    /**
     * Prints the names and IDs of all the contacts in the database in
     * alphabetic order.
     *
     * @pre true
     */
    public void displayDB() {
        // not yet ordered
        System.out.println("capacity " + table.length + " size " + numEntries
                + " Load factor " + loadFactor() + "%");
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null) 
                System.out.println(i + " " + table[i].toString());
            else
                 System.out.println(i + " " + "_____");
            }
        
        
        Contact[] toBeSortedTable = new Contact[tableCapacity];  // OK to use Array.sort
        int j = 0;
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null) {
                toBeSortedTable[j] = table[i];
                j++;
            }
        }
        quicksort(toBeSortedTable, 0, j - 1);
        for (int i = 0; i != j; i++) {
            System.out.println(i + " " + " " + toBeSortedTable[i].toString());
        }
    }

    private void quicksort(Contact[] a, int low, int high) {
        assert a != null && 0 <= low && low <= high && high < a.length;
        int i = low, j = high;
        Contact temp;
        if (high >= 0) { // can't get pivot for empty sequence
            String pivot = a[(low + high) / 2].getName();
            while (i <= j) {
                while (a[i].getName().compareTo(pivot) < 0) i++;
                while (a[j].getName().compareTo(pivot) > 0) j--;
                // forall k :low ..i -1: a[k] < pivot && 
                // forall k: j+1 .. high: a[k] > pivot &&
                // a[i] >= pivot && a[j] <= pivot
                if (i <= j) {
                    temp = a[i]; a[i] = a[j]; a[j] = temp;
                    i++; j--;
                }
                if (low < j) quicksort(a, low, j); // recursive call 
                if (i < high) quicksort(a, i, high); // recursive call 
            }
        }
    }

    private void resizeTable() { // mkae a new table of greater capacity and rehashes old values into it
        System.out.println("RESIZING");
        Contact[] oldTable = table; // copy the reference
        int oldTableCapacity = tableCapacity;
        tableCapacity = oldTableCapacity * 2;
        System.out.println("resizing to " + tableCapacity);
        table = new Contact[tableCapacity]; // make a new tyable
        
        clearDB();
        numEntries = 0;
        for (int i = 0; i != oldTableCapacity; i++) {
            if (oldTable[i] != null) { // dleted vakues not hashed across
                putWithoutResizing(oldTable[i]);
            }
        }
        
    }
} 

