import java.util.ArrayList;
import java.util.HashMap;

public class OrderStatistics<K extends Comparable<K>, T> {
    private static class TreeNode<K extends Comparable<K>, T> {
        private K key;
        private T val;
        private boolean isRed;
        private TreeNode<K, T> left, right, parent;
        private int weight;

        TreeNode(K key, T val, TreeNode<K, T> parent) {
            this.key = key;
            this.val = val;
            this.parent = parent;
            this.weight = 1;
            this.isRed = true;
        }
    }

    TreeNode<K, T> root;

    OrderStatistics(K key, T val) {
        this.root = new TreeNode<>(key, val, null);
        this.root.isRed = false;
    }

    OrderStatistics() {

    }

    /*
     * Rank
     */
    public int rank(K key) {
        return rank(root, key, 0);
    }

    private int rank(TreeNode<K, T> root, K key, int acc) {
        if (root == null) {
            return acc;
        }
        if (key.compareTo(root.key) <= 0) {
            return rank(root.left, key, acc);
        } else {
            return rank(root.right, key, acc + getWeight(root.left) + 1);
        }
    }

    /*
     * Deletion
     * - delete(key)
     * - search(root, node)
     * - delete(node)
     * - deleteBlackNonRoot(node)
     */

    public void delete(K key) {
        TreeNode<K, T> node = search(root, key);
        if (node == null) {
            return;
        }

        boolean isLeaf = node.left == null && node.right == null;

        TreeNode<K, T> weightUpdate = delete(node);

        // fix all the weights up to root
        while (weightUpdate != null) {
            updateWeights(weightUpdate);
            weightUpdate = weightUpdate.parent;
        }

        if (isLeaf && !isRed(node)) {
            fixBlackNonRoot(node.parent);
        }

        while (root.parent != null) {
            root = root.parent;
        }
    }

    private TreeNode<K, T> search(TreeNode<K, T> root, K key) {
        if (root == null) {
            return null;
        }
        if (root.key.equals(key)) {
            return root;
        }

        if (key.compareTo(root.key) <= 0) {
            return search(root.left, key);
        }

        return search(root.right, key);
    }

    private TreeNode<K, T> delete(TreeNode<K, T> node) {
        TreeNode<K, T> weightUpdate = node;

        if (node.left != null && node.right != null) {
            TreeNode<K, T> succ = node.right;
            while (succ.left != null) {
                succ = succ.left;
            }

            node.key = succ.key;
            node.val = succ.val;
            weightUpdate = delete(succ);
        } else if (node.right != null) {
            node.right.isRed = false;

            if (node.parent != null) {
                if (node == node.parent.left) {
                    node.parent.left = node.right;
                } else {
                    node.parent.right = node.right;
                }
            }
            setParent(node.right, node.parent);
            weightUpdate = node.right;

        } else if (node.left != null) {
            node.left.isRed = false;

            if (node.parent != null) {
                if (node == node.parent.left) {
                    node.parent.left = node.left;
                } else {
                    node.parent.right = node.left;
                }
            }

            setParent(node.left, node.parent);
            weightUpdate = node.left;

            // no children for remaining cases
        } else if (node == root) {
            this.root = null;
            return null;
        } else {
            if (node == node.parent.left) {
                node.parent.left = null;
            } else {
                node.parent.right = null;
            }

            weightUpdate = node.parent;
        }

        return weightUpdate;
    }

    private void fixBlackNonRoot(TreeNode<K, T> node) {
        TreeNode<K, T> parent = node.parent,
                sibling,
                closeNephew,
                distantNephew;

        while (parent != null) {

            // initially, we were black, so
            // we could not have been an only
            // child. One side is non-null.
            if (node == parent.left) {
                sibling = parent.right;
                closeNephew = sibling.left;
                distantNephew = sibling.right;
            } else {
                sibling = parent.left;
                closeNephew = sibling.right;
                distantNephew = sibling.left;
            }

            if (isRed(sibling)) {
                // now we have black (node) <- black -> red (sibling) -> 2 blacks
                // so we rotate sibling up to get red root
                // then we push the red down to former parent
                if (sibling == parent.left) {
                    rotateRight(parent);
                    sibling = closeNephew;
                    distantNephew = sibling.left;
                    closeNephew = sibling.right;
                } else { // (sibling == parent.right)
                    rotateLeft(parent);
                    sibling = closeNephew;
                    distantNephew = sibling.right;
                    closeNephew = sibling.left;
                }
                parent.isRed = true;
                parent.parent.isRed = false;


                if (isRed(closeNephew) && !isRed(distantNephew)) {
                    solveOnlyCloseNephewRed(
                            node, parent, sibling, closeNephew, distantNephew
                    );

                    return;
                }

                if (isRed(distantNephew)) {
                    solveOnlyDistantNephewRed(
                            node, parent, sibling, closeNephew, distantNephew
                    );

                    return;
                }

                sibling.isRed = true;
                parent.isRed = false;
                return;
            }

            if (isRed(distantNephew)) {
                solveOnlyDistantNephewRed(
                        node, parent, sibling, closeNephew, distantNephew
                );
                return;
            }

            if (isRed(closeNephew)) {
                solveOnlyCloseNephewRed(
                        node, parent, sibling, closeNephew, distantNephew
                );
                return;
            }

            if (node == root) {
                return;
            }

            if (isRed(parent)) {
                sibling.isRed = true;
                parent.isRed = false;
                return;
            }

            sibling.isRed = true;
            node = parent;
            parent = node.parent;
        }
    }

    private void solveOnlyCloseNephewRed(
            TreeNode<K, T> node,
            TreeNode<K, T> parent,
            TreeNode<K, T> sibling,
            TreeNode<K, T> closeNephew,
            TreeNode<K, T> distantNephew) {

        if (closeNephew == sibling.left) {
            sibling = rotateRight(sibling);

            // closeNephew was on the left, so I am on the left
            closeNephew = sibling.left;
            distantNephew = sibling.right;

        } else { // closeNephew == sibling.right
            sibling = rotateLeft(sibling);

            // similarly I must be on the right now
            closeNephew = sibling.right;
            distantNephew = sibling.left;
        }

        // distantNephew is non-null because it was
        // formerly sibling with a red child
        sibling.isRed = false;
        distantNephew.isRed = true;

        solveOnlyDistantNephewRed(node, parent, sibling, closeNephew, distantNephew);
    }

    private void solveOnlyDistantNephewRed(
            TreeNode<K, T> node,
            TreeNode<K, T> parent,
            TreeNode<K, T> sibling,
            TreeNode<K, T> closeNephew,
            TreeNode<K, T> distantNephew) {
        if (closeNephew == sibling.left) {
            // aka node == parent.left
            rotateLeft(parent);
        }
        else {
            rotateRight(parent);
        }

        sibling.isRed = parent.isRed;
        parent.isRed = false;
        distantNephew.isRed = false;

    }


    /*
     * Insertion
     * - insert(key, value)
     * - insert(root, node)
     * - findInsert(root, node)
     * - fixWeights(node)
     */
    public void insert(K key, T val) {
        insert(new TreeNode<>(key, val, null));
    }

    private void insert(TreeNode<K, T> node) {
        findInsert(root, node);
        fixInsert(node);

        while (node.parent != null) {
            node = node.parent;
        }

        root = node;
        root.isRed = false;
    }

    private TreeNode<K, T> findInsert(TreeNode<K, T> root, TreeNode<K, T> node) {
        if (root == null || node == null) {
            return node;
        }
        node.parent = root;
        root.weight += 1;

        if (leftSmaller(root, node)) {
            root.right = findInsert(root.right, node);
        } else {
            root.left = findInsert(root.left, node);
        }
        return root;
    }

    private void fixInsert(TreeNode<K, T> node) {
        if (node == null) {
            // recursed up to root
            return;
        }

        TreeNode<K, T> parent = node.parent;

        if (!isRed(parent)) {
            // if parent is black we are done, since we are red
            return;
        }
        if (parent.parent == null) {
            // if grandparent is null,
            // then parent must be the root
            // easy fix
            parent.isRed = false;
            return;
        }

        // note that parent is red,
        // so grandparent and sibling must be black
        // because we assume invariant was maintained.

        TreeNode<K, T> grandparent, uncle;
        grandparent = parent.parent;
        if (parent == grandparent.left) {
            uncle = grandparent.right;
        } else {
            uncle = grandparent.left;
        }

        if (isRed(uncle)) {
            // now node is red, parent is red, and uncle is red
            // by invariant, grandparent is black

            // by the way isRed is declared, uncle cannot be null
            // since it is red
            grandparent.isRed = true;
            parent.isRed = false;
            uncle.isRed = false;
            fixInsert(grandparent);
            return;
        }

        // black uncle and grandparent,
        // so we rotate until we get
        // red (node/parent) <- red (node/parent) -> black (grandparent)
        // and we re-color to be
        // red <- black -> red

        // rotate until red <- red <- black
        // or black -> red -> red first
        // where we point on the lowest red
        boolean isLeftRightChild = (parent == grandparent.left) && (node == parent.right);
        boolean isRightLeftChild = (parent == grandparent.right) && (node == parent.left);
        if (isLeftRightChild) {
            grandparent.left = rotateLeft(parent);
            node = parent;
        } else if (isRightLeftChild) {
            grandparent.right = rotateRight(parent);
            node = parent;
        }

        // now we rotate up as needed
        boolean isLeftLeftChild = (parent == grandparent.left) && (node == parent.left);
        boolean isRightRightChild = (parent == grandparent.right) && (node == parent.right);

        if (isLeftLeftChild) {
            TreeNode<K, T> newGrandparent = rotateRight(grandparent);
            newGrandparent.right.isRed = true;
            newGrandparent.isRed = false;

        } else if (isRightRightChild) {
            TreeNode<K, T> newGrandparent = rotateLeft(grandparent);
            newGrandparent.left.isRed = true;
            newGrandparent.isRed = false;
        }

        // now everything is done from grandparent onwards
        return;
    }

    /*
     * Rotations.
     * Re-weights the affected nodes
     *
     * rotateLeft(root) -> newRoot
     * rotateRight(root) -> newRoot
     *
     */

    private static <A extends Comparable<A>, B> TreeNode<A, B> rotateLeft(TreeNode<A, B> root) {
        TreeNode<A, B>
                oldRootParent = root.parent,
                oldRight = root.right,
                oldRightLeft = oldRight.left;

        // detach oldRight from root and move oldRightLeft over
        root.right = oldRightLeft;
        setParent(oldRightLeft, root);

        // re-attach oldRight as parent of oldRoot
        oldRight.left = root;
        setParent(root, oldRight);

        // reset parent of oldRight
        setParent(oldRight, oldRootParent);

        // update weight of oldRoot first, then newRoot (oldLeft)
        updateWeights(root);
        updateWeights(oldRight);

        if (oldRootParent != null) {
            if (root == oldRootParent.left) {
                oldRootParent.left = oldRight;
            } else {
                oldRootParent.right = oldRight;
            }
        }
        return oldRight;
    }

    private static <A extends Comparable<A>, B> TreeNode<A, B> rotateRight(TreeNode<A, B> root) {
        TreeNode<A, B>
                oldRootParent = root.parent,
                oldLeft = root.left,
                oldLeftRight = oldLeft.right;

        // detach oldLeft from root and move oldLeftRight over
        root.left = oldLeftRight;
        setParent(oldLeftRight, root);

        // re-attach oldLeft as parent of oldRoot
        oldLeft.right = root;
        setParent(root, oldLeft);

        // reset parent of oldLeft
        setParent(oldLeft, oldRootParent);

        // update weight of oldRoot first, then newRoot (oldLeft)
        updateWeights(root);
        updateWeights(oldLeft);

        if (oldRootParent != null) {
            if (root == oldRootParent.left) {
                oldRootParent.left = oldLeft;
            } else {
                oldRootParent.right = oldLeft;
            }
        }

        return oldLeft;
    }

    /*
     * Helper functions below (to absorb null cases)
     *
     * updateWeight(node)
     * getWeight(node)
     * isRed(node)
     * setParent(node, parent)
     * leftSmaller(left, right)
     */

    private static <A extends Comparable<A>, B> void updateWeights(TreeNode<A, B> node) {
        if (node == null) {
            throw new RuntimeException("Tried to update weight of null");
        }
        node.weight = getWeight(node.left) + getWeight(node.right) + 1;
    }

    private static <A extends Comparable<A>, B> int getWeight(TreeNode<A, B> node) {
        if (node == null) {
            return 0;
        }
        return node.weight;
    }

    private static <A extends Comparable<A>, B> boolean isRed(TreeNode<A, B> node) {
        if (node == null) {
            return false;
        }
        return node.isRed;
    }

    private static <A extends Comparable<A>, B> void setParent(TreeNode<A, B> child, TreeNode<A, B> parent) {
        if (child == null) {
            return;
        }
        child.parent = parent;
    }

    private static <A extends Comparable<A>, B> boolean leftSmaller(TreeNode<A, B> left, TreeNode<A, B> right) {
        if (left == null || left.key == null) {
            return true;
        }
        if (right == null || right.key == null) {
            return false;
        }
        boolean check = (left.key.compareTo(right.key) <= 0);

        return (left.key.compareTo(right.key) <= 0);
    }

    /*
     * Testing
     *
     * - allLeavesDistance
     *
     */
    public ArrayList<String> inOrder() {
        if (root == null) {
            return null;
        }

        ArrayList<String> stuffs = new ArrayList<>();
        ArrayList<TreeNode<K, T>> next = new ArrayList<>();
        HashMap<TreeNode<K, T>, Boolean> visited = new HashMap<>();
        next.add(root);

        // dfs through all leaves
        while (!next.isEmpty()) {
            TreeNode<K, T> curr = next.removeLast();

            while (curr.left != null && !visited.containsKey(curr.left)) {
                next.add(curr);
                curr = curr.left;
            }
            stuffs.add(String.format("(key: %s, parent: %s, left: %s, right: %s, isRed: %b)\n",
                    curr.key,
                    curr.parent == null? null : curr.parent.key,
                    curr.left == null ? null : curr.left.key,
                    curr.right == null ? null : curr.right.key,
                    curr.isRed));

            visited.put(curr, true);
            if (curr.right != null && !visited.containsKey(curr.right)) {
                next.add(curr.right);
            }
        }

        return stuffs;
    }

    public ArrayList<Integer> allLeavesDistance() {
        if (root == null) {
            return null;
        }

        ArrayList<Integer> distances = new ArrayList<>();
        ArrayDeque<TreeNode<K, T>> next = new ArrayDeque<>();
        ArrayList<String> leaf = new ArrayList<>();
        HashMap<TreeNode<K, T>, Integer> visited = new HashMap<>();
        next.addLast(root);


        visited.put(null, 0);
        // dfs through all leaves
        while (!next.isEmpty()) {
            TreeNode<K, T> curr = next.removeFirst();

            if (visited.getOrDefault(curr, 0) != 0) {
                continue;
            }

            int extraBlack = curr.isRed ? 0 : 1;
            int currBlackDist = visited.get(curr.parent) + extraBlack;
            visited.put(curr, currBlackDist);

            assert(!(isRed(curr) && isRed(curr.parent)));

            if (curr.left == null && curr.right == null) {
                distances.add(currBlackDist);
                leaf.add(curr.key.toString());
            }
            if (curr.left != null) {
                next.addLast(curr.left);
            }
            if (curr.right != null) {
                 next.addLast(curr.right);
            }
        }
//        System.out.println(leaf);
        return distances;
    }

}
