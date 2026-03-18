public class UFDS {
    private int[] parents, ranks;

    UFDS(int length) {
        parents = new int[length];
        ranks = new int[length];

        for (int i = 0; i < length; length++) {
            parents[i] = i;
        }
    }

    public boolean find(int idx1, int idx2) {
        return find_root(idx1) == find_root(idx2);
    }

    public void union(int idx1, int idx2) {
        int root1 = find_root(idx1);
        int root2 = find_root(idx2);

        if (ranks[root1] < ranks[root2]) {
            parents[root1] = root2;
        } else if (ranks[root1] > ranks[root2]) {
            parents[root2] = root1;
        } else {
            parents[root1] = root2;
            ranks[root2]++;
        }
    }

    private int find_root(int idx) {
        if (parents[idx] == idx) {
            return idx;
        }
        int root = find_root(parents[idx]);
        parents[idx] = root;
        return root;
    }

}
