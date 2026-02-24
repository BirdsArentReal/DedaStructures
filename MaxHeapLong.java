public class MaxHeapLong {
        private long[] keys, id_to_pos, pos_to_id, id_to_value;
        private int nextID;

        private static final int defaultSize = 10;


        public MaxHeapLong() {
            this.keys = initArray(defaultSize);
            this.id_to_value = initArray(defaultSize);
            this.id_to_pos = initArray(defaultSize);
            this.pos_to_id = initArray(defaultSize);
            this.nextID = 1;
        }

        public void insert(long key, long value) {
            int currID = nextID;
            nextID++;

            if (nextID >= id_to_pos.length) {
                doubleSize();
            }

            // update size
            keys[0]++;
            int nextPos = (int) keys[0];

            id_to_value[currID] = value;
            id_to_pos[currID] = nextPos;
            pos_to_id[nextPos] = currID;
            keys[nextPos] = key;

            bubbleUp(nextPos);
        }

        public long[] removeMax() {
            if (this.size() == 0) {
                System.out.println("Tried to remove from empty heap");
                return new long[]{Long.MIN_VALUE, Long.MIN_VALUE};
            }

            long[] task = new long[]{peekKeys(), peekValue()};

            keys[1] = Long.MIN_VALUE;
            id_to_value[(int) pos_to_id[1]] = Long.MIN_VALUE;
            id_to_pos[(int) pos_to_id[1]] = Long.MIN_VALUE;
            pos_to_id[1] = Long.MIN_VALUE;

            swap(1, (int) keys[0]);
            keys[0]--;

            bubbleDown(1);

            return task;
        }

        public long peekKeys() {
            return keys[1];
        }

        public long peekValue() {
            return id_to_value[(int) pos_to_id[1]];
        }

        public int size() {
            return (int) keys[0];
        }

        private void bubbleUp(int childPos) {
            if (childPos == 1) {
                // already at root, no more bubbling
                return;
            }
            int parentPos = childPos / 2;

            if (firstBiggerThanSecond(childPos, parentPos)) {
                swap(childPos, parentPos);
                bubbleUp(parentPos);
            }
        }

        private void bubbleDown(int parentPos) {
            if (2 * parentPos >= keys.length) {
                // only need to check left child, because last index
                // of my array is always odd

                // this means parentPos is already a leaf node
                return;
            }

            int leftChild = 2 * parentPos;
            int rightChild = 2 * parentPos + 1;

            int maxChild = firstBiggerThanSecond(leftChild, rightChild) ? leftChild : rightChild;

            if (firstBiggerThanSecond(maxChild, parentPos)) {
                swap(maxChild, parentPos);
                bubbleDown(maxChild);
            }
        }

        private boolean firstBiggerThanSecond(int pos1, int pos2) {

            long firstKey = keys[pos1];
            long secondKey = keys[pos2];

            long firstValue = id_to_value[(int) pos_to_id[pos1]];
            long secondValue = id_to_value[(int) pos_to_id[pos2]];

            boolean firstMoreEnergy = (firstKey > secondKey);
            boolean sameEnergy = (firstKey == secondKey);
            boolean moreGold = (firstValue > secondValue);

            return firstMoreEnergy || (sameEnergy && moreGold);
        }

        private void swap(int pos1, int pos2) {
            long oldPos1Key = keys[pos1];
            int oldPos1ID = (int) pos_to_id[pos1];
            int oldPos2ID = (int) pos_to_id[pos2];

            // swap keys
            keys[pos1] = keys[pos2];
            keys[pos2] = oldPos1Key;

            // update id_to_pos
            id_to_pos[oldPos2ID] = pos1;
            id_to_pos[oldPos1ID] = pos2;

            // update pos_to_id
            pos_to_id[pos1] = oldPos2ID;
            pos_to_id[pos2] = oldPos1ID;
        }

        private void doubleSize() {
            // we can't ever halve the size because of id_to_pos
            // and id_to_value
            int size = keys.length;
            long[] newKeys = initArray(2 * size),
                    newId_to_value = initArray(2 * size),
                    newId_to_pos = initArray(2 * size),
                    newPos_to_id = initArray(2 * size);

            for (int i = 0; i < size; i++) {
                newKeys[i] = keys[i];
                newId_to_pos[i] = id_to_pos[i];
                newId_to_value[i] = id_to_value[i];
                newPos_to_id[i] = pos_to_id[i];
            }

            keys = newKeys;
            id_to_value = newId_to_value;
            id_to_pos = newId_to_pos;
            pos_to_id = newPos_to_id;
        }

        private long[] initArray(int size) {
            long[] arr = new long[size];

            // pos 0 is only used for keys array
            // to store the size information
            arr[0] = 0;
            for (int i = 1; i < size; i++) {
                arr[i] = Long.MIN_VALUE;
            }

            return arr;
        }

        @Override
        public String toString() {
            String s = "[";

            for (long e : keys) {
                s += e + ", ";
            }

            return s + "]";
        }
    }