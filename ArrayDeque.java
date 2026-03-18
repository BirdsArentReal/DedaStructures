public class ArrayDeque<T> {
    private T[] queue;
    private int head, tail, size;

    ArrayDeque() {
        this((T[])new Object[10]);
    }

    ArrayDeque(T... items) {
        this.queue = items;
        this.head = 0;
        this.tail = 0;
        this.size = 0;

        if (this.queue == null) {
            this.queue = (T[]) new Object[10];
        }
    }
    public void enqueue(T item) {
        addLast(item);
    }

    public void push(T item) {
        addLast(item);
    }

    public T dequeue() {
        return removeFirst();
    }

    public T pop() {
        return removeLast();
    }

    public T peekFirst() {
        return this.get(0);
    }

    public T peekLast() {
        return this.get(size - 1);
    }

    public void addFirst(T item) {
        if (this.isFull()) {
            doubleSize();
        }

        this.head = (this.head - 1) % this.queue.length;
        this.queue[this.head] = item;
        this.size++;
    }

    public void addLast(T item) {
        if (this.isFull()) {
            doubleSize();
        }

        this.queue[this.tail] = item;
        this.tail = (this.tail + 1) % this.queue.length;
        this.size++;
    }

    public T removeFirst() {
        T item = this.queue[this.head];
        this.head = (this.head + 1) % this.queue.length;
        this.size--;

        if (this.size * 4 <= this.queue.length) {
            halveSize();
        }

        return item;
    }

    public T removeLast() {
        T item = this.queue[this.tail];
        this.tail = (this.tail - 1) % this.queue.length;
        this.size--;

        if (this.size * 4 <= this.queue.length) {
            halveSize();
        }

        return item;
    }

    public boolean isFull() {
        return this.size == this.queue.length;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private T get(int idx) {
        return this.queue[(this.head + idx) % this.queue.length];
    }

    private void doubleSize() {
        T[] temp = (T[]) new Object[this.queue.length * 2];

        for (int i = 0; i < this.size; i++) {
            temp[i] = get(i);
        }

        this.head = 0;
        this.tail = this.size;
        this.queue = temp;
    }

    private void halveSize() {
        if (this.size < 10) {
            return;
        }
        T[] temp = (T[]) new Object[this.queue.length / 2];

        for (int i = 0; i < this.size; i++) {
            temp[i] = get(i);
        }

        this.head = 0;
        this.tail = this.size;
        this.queue = temp;
    }
}
