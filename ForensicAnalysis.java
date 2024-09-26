package forensic;

import java.util.LinkedList;

/**
 * This class represents a forensic analysis system that manages DNA data using
 * BSTs.
 * Contains methods to create, read, update, delete, and flag profiles.
 * 
 * @author Kal Pandit
 */
public class ForensicAnalysis {

    private TreeNode treeRoot;            // BST's root
    private String firstUnknownSequence;
    private String secondUnknownSequence;

    public ForensicAnalysis () {
        treeRoot = null;
        firstUnknownSequence = null;
        secondUnknownSequence = null;
    }

    /**
     * Builds a simplified forensic analysis database as a BST and populates unknown sequences.
     * The input file is formatted as follows:
     * 1. one line containing the number of people in the database, say p
     * 2. one line containing first unknown sequence
     * 3. one line containing second unknown sequence
     * 2. for each person (p), this method:
     * - reads the person's name
     * - calls buildSingleProfile to return a single profile.
     * - calls insertPerson on the profile built to insert into BST.
     *      Use the BST insertion algorithm from class to insert.
     * 
     * DO NOT EDIT this method, IMPLEMENT buildSingleProfile and insertPerson.
     * 
     * @param filename the name of the file to read from
     */
    public void buildTree(String filename) {
        // DO NOT EDIT THIS CODE
        StdIn.setFile(filename); // DO NOT remove this line

        // Reads unknown sequences
        String sequence1 = StdIn.readLine();
        firstUnknownSequence = sequence1;
        String sequence2 = StdIn.readLine();
        secondUnknownSequence = sequence2;
        
        int numberOfPeople = Integer.parseInt(StdIn.readLine()); 

        for (int i = 0; i < numberOfPeople; i++) {
            // Reads name, count of STRs
            String fname = StdIn.readString();
            String lname = StdIn.readString();
            String fullName = lname + ", " + fname;
            // Calls buildSingleProfile to create
            Profile profileToAdd = createSingleProfile();
            // Calls insertPerson on that profile: inserts a key-value pair (name, profile)
            insertPerson(fullName, profileToAdd);
        }
    }

    /** 
     * Reads ONE profile from input file and returns a new Profile.
     * Do not add a StdIn.setFile statement, that is done for you in buildTree.
    */
    public Profile createSingleProfile() {

        int numOfSTRs = StdIn.readInt();
        STR[] strs = new STR[numOfSTRs];

        for (int i = 0; i < numOfSTRs; i++) {
            String strName = StdIn.readString();
            int occurrences = StdIn.readInt();
            strs[i] = new STR(strName, occurrences);
        }
        
        return new Profile(strs); // update this line
    }

    /**
     * Inserts a node with a new (key, value) pair into
     * the binary search tree rooted at treeRoot.
     * 
     * Names are the keys, Profiles are the values.
     * USE the compareTo method on keys.
     * 
     * @param newProfile the profile to be inserted
     */
    public void insertPerson(String name, Profile newProfile) {

        treeRoot = insert(treeRoot, name, newProfile);
    }

    public TreeNode insert(TreeNode node, String name, Profile profile) {
        if (node == null) return new TreeNode(name, profile, node, node);

        int cmp = name.compareTo(node.getName());
        if (cmp < 0) node.setLeft(insert(node.getLeft(), name, profile));
        else if (cmp > 0) node.setRight(insert(node.getRight(), name, profile));

        return node;

    }

    /**
     * Finds the number of profiles in the BST whose interest status matches
     * isOfInterest.
     *
     * @param isOfInterest the search mode: whether we are searching for unmarked or
     *                     marked profiles. true if yes, false otherwise
     * @return the number of profiles according to the search mode marked
     */
    public int getMatchingProfileCount(boolean isOfInterest) {
        
        Queue<TreeNode> queue = new Queue<TreeNode>();
    queue.enqueue(treeRoot);
    int count = 0;
    while (!queue.isEmpty()) {
        TreeNode node = queue.dequeue();
        if (node != null) {
            Profile p = node.getProfile();
            if (p.getMarkedStatus() == isOfInterest) {
                count++;
            }
            queue.enqueue(node.getLeft());
            queue.enqueue(node.getRight());
        }
    }
    return count;
}

    /**
     * Helper method that counts the # of STR occurrences in a sequence.
     * Provided method - DO NOT UPDATE.
     * 
     * @param sequence the sequence to search
     * @param STR      the STR to count occurrences of
     * @return the number of times STR appears in sequence
     */
    private int numberOfOccurrences(String sequence, String STR) {
        
        // DO NOT EDIT THIS CODE
        
        int repeats = 0;
        // STRs can't be greater than a sequence
        if (STR.length() > sequence.length())
            return 0;
        
            // indexOf returns the first index of STR in sequence, -1 if not found
        int lastOccurrence = sequence.indexOf(STR);
        
        while (lastOccurrence != -1) {
            repeats++;
            // Move start index beyond the last found occurrence
            lastOccurrence = sequence.indexOf(STR, lastOccurrence + STR.length());
        }
        return repeats;
    }

    /**
     * Traverses the BST at treeRoot to mark profiles if:
     * - For each STR in profile STRs: at least half of STR occurrences match (round
     * UP)
     * - If occurrences THROUGHOUT DNA (first + second sequence combined) matches
     * occurrences, add a match
     */
    private void traverseBST(TreeNode node, Queue<Profile> queue) {
        if (node == null) {
            return;
        }
        traverseBST(node.getLeft(), queue);
        queue.enqueue(node.getProfile());
        traverseBST(node.getRight(), queue);
    }
    
    public void flagProfilesOfInterest() {
        Queue<Profile> profileQueue = new Queue<Profile>();
        traverseBST(treeRoot, profileQueue);
    
        while (!profileQueue.isEmpty()) {
            Profile currentProfile = profileQueue.dequeue();
            STR[] strArray = currentProfile.getStrs();
            int matchingStrCount = 0;
    
            for (STR str : strArray) {
                int occurrencesInFirst = numberOfOccurrences(firstUnknownSequence, str.getStrString());
                int occurrencesInSecond = numberOfOccurrences(secondUnknownSequence, str.getStrString());
                if (str.getOccurrences() == occurrencesInFirst + occurrencesInSecond) {
                    matchingStrCount++;
                }
            }
    
            if (matchingStrCount >= Math.ceil(strArray.length / 2.0)) {
                currentProfile.setInterestStatus(true);
            }
        }
    }

    /**
     * Uses a level-order traversal to populate an array of unmarked Strings representing unmarked people's names.
     * - USE the getMatchingProfileCount method to get the resulting array length.
     * - USE the provided Queue class to investigate a node and enqueue its
     * neighbors.
     * 
     * @return the array of unmarked people
     */
    public String[] getUnmarkedPeople() { 

        int counter = getMatchingProfileCount(false);
    String[] unmarked = new String[counter];
    int index = 0;

    Queue<TreeNode> queue = new Queue<>();
    queue.enqueue(treeRoot);

    while (!queue.isEmpty()) {
        TreeNode node = queue.dequeue();
        if (node != null) {
            Profile p = node.getProfile();
            if (!p.getMarkedStatus()) {
                unmarked[index++] = node.getName();
            }
            queue.enqueue(node.getLeft());
            queue.enqueue(node.getRight());
        }
    }
    return unmarked; // update this line
} 
    

    /**
     * Removes a SINGLE node from the BST rooted at treeRoot, given a full name (Last, First)
     * This is similar to the BST delete we have seen in class.
     * 
     * If a profile containing fullName doesn't exist, do nothing.
     * You may assume that all names are distinct.
     * 
     * @param fullName the full name of the person to delete
     */
    public void removePerson(String fullName) {
        treeRoot = removePersonFromTree(treeRoot, fullName);
}

private TreeNode removePersonFromTree(TreeNode currentNode, String targetName) {
    if (currentNode == null) {
        return null;
    }

    int comparisonResult = targetName.compareTo(currentNode.getName());

    if (comparisonResult < 0) {
        currentNode.setLeft(removePersonFromTree(currentNode.getLeft(), targetName));
    } else if (comparisonResult > 0) {
        currentNode.setRight(removePersonFromTree(currentNode.getRight(), targetName));
    } else {
        if (currentNode.getLeft() == null) {
            return currentNode.getRight();
        } else if (currentNode.getRight() == null) {
            return currentNode.getLeft();
        } else {
            TreeNode successorNode = findMinimumNode(currentNode.getRight());
            currentNode.setName(successorNode.getName());
            currentNode.setProfile(successorNode.getProfile());
            currentNode.setRight(deleteMinimumNode(currentNode.getRight()));
        }
    }

    return currentNode;
}

private TreeNode findMinimumNode(TreeNode rootNode) {
    while (rootNode.getLeft() != null) {
        rootNode = rootNode.getLeft();
    }
    return rootNode;
}

private TreeNode deleteMinimumNode(TreeNode rootNode) {
    if (rootNode.getLeft() == null) {
        return rootNode.getRight();
    }
    rootNode.setLeft(deleteMinimumNode(rootNode.getLeft()));
    return rootNode;
}


    /**
     * Clean up the tree by using previously written methods to remove unmarked
     * profiles.
     * Requires the use of getUnmarkedPeople and removePerson.
     */
    public void cleanupTree() {
        String[] unmarkedPeople = getUnmarkedPeople();
    for (String person : unmarkedPeople) {
        removePerson(person);
    }
}

    /**
     * Gets the root of the binary search tree.
     *
     * @return The root of the binary search tree.
     */
    public TreeNode getTreeRoot() {
        return treeRoot;
    }

    /**
     * Sets the root of the binary search tree.
     *
     * @param newRoot The new root of the binary search tree.
     */
    public void setTreeRoot(TreeNode newRoot) {
        treeRoot = newRoot;
    }

    /**
     * Gets the first unknown sequence.
     * 
     * @return the first unknown sequence.
     */
    public String getFirstUnknownSequence() {
        return firstUnknownSequence;
    }

    /**
     * Sets the first unknown sequence.
     * 
     * @param newFirst the value to set.
     */
    public void setFirstUnknownSequence(String newFirst) {
        firstUnknownSequence = newFirst;
    }

    /**
     * Gets the second unknown sequence.
     * 
     * @return the second unknown sequence.
     */
    public String getSecondUnknownSequence() {
        return secondUnknownSequence;
    }

    /**
     * Sets the second unknown sequence.
     * 
     * @param newSecond the value to set.
     */
    public void setSecondUnknownSequence(String newSecond) {
        secondUnknownSequence = newSecond;
    }

}
