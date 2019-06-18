class LRUCache {

    class Node {
        int key;
        Node prev;
        Node next;
        
        public Node(int key) {
            this.key = key;
            this.prev = null;
            this.next = null;
        }
    }
    
    private int capacity;
    private Map<Integer, Integer> cache;
    private Node head = new Node(-1);
    private Node tail = new Node(-1);
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<Integer, Integer>(); 
        this.head.next = tail;
        this.tail.prev = head;
    }
    
    public int get(int key) {
        if (cache.containsKey(key)) {
            moveToFront(key);
            return cache.get(key);
        } else {
            return -1;
        }
    }
    
    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            cache.replace(key, cache.get(key), value);
            moveToFront(key);
        } else {
            if (checkCapacity() == true) {
                cache.remove(tail.prev.key);
                deleteLRU();
            }
            prepend(key);
            cache.put(key, value);
        }
    }
    
    private void moveToFront(int key) {
        Node current = head;

        while (current != tail) {
            if (current.key == key) {
                current.next.prev = current.prev;
                current.prev.next = current.next;
                break;
            }
            current = current.next;
        }
        
        prepend(current.key);
    }
    
    private boolean checkCapacity() {
        return cache.size() >= capacity;
    }
    
    private void deleteLRU() {
        Node lruNode = getLRU();
        tail.prev = lruNode.prev;
        lruNode.prev.next = tail;
    }
    
    private Node getLRU() {
        return tail.prev;
    }
    
    private void prepend(int key) {
        Node newNode = new Node(key);
        
        if (head.next == tail) {
            head.next = newNode;
            tail.prev = newNode;
            newNode.prev = head;
            newNode.next = tail;
        } else {            
            newNode.next = head.next;
            head.next.prev = newNode;
            head.next = newNode;
            newNode.prev = head;
        }
    }
}