package cw1a;

/**
 *
 * @author DL 2025-01
 */
public class ContactsHashOpen implements IContactDB {  
    private static final int[] PRIME_SIZES = {
        1009, 2003, 4001, 8009, 16001, 32003, 64007, 128021
    };
    private int primeIndex = 0;
    private Contact[] table;
    private int tableCapacity;
    private int numEntries;
    private int totalVisited = 0;
    /**Declare a static final object acting as sentinel for marking deleted entries in the hash table
     * This allows for null slots (never used), regular Contact objects (i.e. placed in 'active' slots),
     * and deleted slots (populated by the 'DELETED' sentinel objects) to be distinguished. 
     * As such, proper probe sequencing is maintained. 
     */
    private static final Contact DELETED = new Contact("__DELETED__","0");

    private static final double maxLoadFactor = 50.0;
            
    public int getNumEntries(){return numEntries;}
    public void resetTotalVisited() {totalVisited = 0;}
    public int getTotalVisited() {return totalVisited;}

    public ContactsHashOpen() {
        System.out.println("Hash Table with open addressing");
        this.tableCapacity = PRIME_SIZES[primeIndex];
        table = new Contact[tableCapacity];
        clearDB();
    }

    /**
     * Empties the database, while preserving the markers for 'lasily DELETED' 
     * Contact objects in the appropriate slots. This faciliates maintenance of
     * probe sequence structure and, if built upon in future could help to
     * optimise future insertions.
     * Further, load factor calculations must account for prior deletions, so
     * tabs must be kept on deleted entries to accomodate their functionality.
     *
     * @pre true
     */
    public void clearDB() {
        for (int i = 0; i != table.length; i++) {
            if (table[i] != DELETED){ //only clear entries that have not been deleted
                table[i] = null;
            }
        }
        numEntries = 0;
    }
        // MODIFIED HASH FUNCTION 1st WORKING DRAFT

    private int hash(String s) {
        assert  s != null && !s.trim().equals(""); 
        System.out.println("Hashing string: '" + s + "'");
        final int PRIME = 31;
        int hash = s.length() * s.charAt(0);//combine the length with the first character
        
        for (int i = 1; i < s.length(); i++){ //loop through from 2nd character 
            hash = (hash*PRIME + s.charAt(i))%table.length;
        }
        
        return Math.abs(hash);   
        
    }

    private double loadFactor() {
        return (double) numEntries / (double) table.length * 100.0;
    } 
       
    /**Locates the position in the hash table for a given Contact object denoted
     * by name. 'Lazy deletion' implemented alongside quadratic probing.
     * Maintains neccessary chains for collision resolution with quad probing.
     * Could be augmented in future to minimise clustering by 'remembering',
     * for possible re-use, the deleted positions.
     * 
     * @param name : the name of the Contact object to be located in the HT
     * @return : the position where that name is found, or the first truly empty
     * (null) encountered while probing where the name may be inserted to the HT.
     **/
    private int findPos(String name) {
        assert name != null && !name.trim().equals(""); //trim whitespace and verify name is not null or an empty string
        int startPos = hash(name); //Calculate initial hash position 
        int pos = startPos; //init the position being examined to the initial hash position
        int numVisited = 1; //init counter for num positions checked
        //Debugger print statements
        
        System.out.println("\nStarting search for: " + name);
        System.out.println("Initial hash position: " + startPos);
        

        
        //check whether the initial position is either empy (null - maybe change
        //later...), or contains target.
        //If so for either, no probing needed so can skip to return
        if (table[pos] == null){
            System.out.println("Search ended at position " + pos + "(empty)");
            totalVisited += numVisited;
            return pos;
            
        //or, if the target was found at the initial position, and has not been
        //deleted, no need for probing either, so return that position.     
        } else if (table[pos]!= DELETED && name.equals(table[pos].getName())) {
                System.out.println("Search ended at position " + pos + "(found: " + table[pos].getName() + ")");
                totalVisited += numVisited;
                return pos;                
        }

        //commence quad probing if the initial position is not appropriate.
        for (int i=1; numVisited < table.length; i++){
            //keep track of bucket trail for analysis
           System.out.println("Visiting bucket " + pos + ": " + table[pos] );
           
           /**apply the quadratic formula h(k,i) = (h'(k) + i^2) mod m , where:
            * h'(k) is the initit hash value for key, denoted by startPos
            * i denotes the number representing the current probe attempt count
            * i^2 represents the step size, facilitating quadratic expansion 
            * m is the table capacity (set elsewhere to prime), i.e. table.length
            * the result being the next pos in the table to be checked.
            */
           int step = i * i;
           //Calculate the new position using quad formula
           //Double up the mod to ensure positive result even if overflow leads
           //the first statement to yield a negative
           pos = ((startPos + step) % table.length + table.length) % table.length;
           numVisited++;//inc counter 
           //Debugger print stmnt
           System.out.println("Next step size: " + step + ",moving to position: " + pos);
           
           if (table[pos] == null){
              break;
           //or, if the target has been found...    
           }else if (table[pos] != DELETED && name.equals(table[pos].getName())){
               break;
              
           }
           
        }
        //output trail and result for analysis  
        //System.out.print("Search ended at position " +pos);
        if (table[pos] != null && table[pos] != DELETED){
            System.out.println("Found " + table[pos].getName());
        } else {
            System.out.println("Position is empty or has been deleted");
        }
        System.out.println("number of  buckets visited = " + numVisited);
        totalVisited += numVisited;
      
        //assert table[pos] == null || name.equals(table[pos].getName());
        return pos;
    }

    /**
     * Determines whether a Contact name exists as a key inside the database
     *
     * @pre name not null or empty string
     * @param name the Contact name (key) to locate
     * @return true iff the name exists as a key in the database to a non-DELETED
     * slot
     */
    public boolean containsName(String name) {
        assert name != null && !name.equals("");
        Contact contact = get(name);
        return contact != null && contact != DELETED;
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
        int pos = findPos(name);
        //null is returned for both empty and DELETED entries
        if (table[pos] == null || table[pos] == DELETED) {
            return null; // not found
        } 
        return  table[pos];       
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
     * supplied name, using lazy deletion.
     *
     * @param name The name (key) to remove.
     * @pre name not null or empty string
     * @return the removed contact object mapped to the name, or null if the
     * name does not exist.
     */
    public Contact remove(String name) {
        assert name != null && !name.trim().equals("");
        int pos = findPos(name);
        Contact removedContact = null; // initialise
        
        //Only 'DELETE' if a non-null, non-DELETED, matching entry
        if (table[pos] != null && table[pos] != DELETED && name.equals(table[pos].getName())){
            removedContact = table[pos];
            table[pos] = DELETED; //rather than using null, mark as DELETED
            numEntries--; //decrement size, replicating the removal of an entry
        }
        
        return removedContact;
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
    /**
     * When loadFactor > the assigned max, resizes the hash table, carrying over 
     * only non-DELETED entries, clearing deletion markers and rehashing all the
     * new entries into the new, expanded table. 
     */
    private void resizeTable() { // mkae a new table of greater capacity and rehashes old values into it
        System.out.println("RESIZING");
        Contact[] oldTable = table; // copy the reference
        int oldTableCapacity = tableCapacity;
        primeIndex++;
        
        tableCapacity = PRIME_SIZES[primeIndex];
        System.out.println("resizing to " + tableCapacity);
        table = new Contact[tableCapacity]; // make a new table
        
        clearDB();
        numEntries = 0;
        for (int i = 0; i != oldTableCapacity; i++) {
            if (oldTable[i] != null && oldTable[i] != DELETED) { // dleted vakues not hashed across
                putWithoutResizing(oldTable[i]);
            }
        }
        
    }
} 

