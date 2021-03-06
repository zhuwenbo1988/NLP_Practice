# coding=utf-8

# https://leetcode-cn.com/problems/construct-binary-tree-from-inorder-and-postorder-traversal/

# Definition for a binary tree node.
# class TreeNode(object):
#     def __init__(self, x):
#         self.val = x
#         self.left = None
#         self.right = None

class Solution(object):
  def buildTree(self, inorder, postorder):
    """
    :type inorder: List[int]
    :type postorder: List[int]
    :rtype: TreeNode
    """
    l = []
    size = len(inorder)
    if size == 0:
      return
    in_ptr = size-1
    post_ptr = size-1
    root = TreeNode(postorder[post_ptr].val)
    curr = root
    is_build_left = False
    post_ptr += -1
    l.insert(0, root)

    while post_ptr > -1:
      if l and l[0].val == inorder[in_ptr]:
        curr = l[0]
        del l[0]
        in_ptr += -1
        is_build_left = True
      else:
        if is_build_left:
          curr.left = TreeNode(postorder[post_ptr].val)
          curr = curr.left
          l.insert(0, curr)
          post_ptr += -1
          is_build_left = False
        else:
          curr.right = TreeNode(postorder[post_ptr].val)
          curr = curr.right
          l.insert(0, curr)
          post_ptr += -1
    return root

'''

递归版本

构建二叉树的问题本质上就是：
找到各个子树的根节点
构建该根节点的左子树
构建该根节点的右子树

'''

class Solution(object):
  def buildTree(self, inorder, postorder):
    """
    :type inorder: List[int]
    :type postorder: List[int]
    :rtype: TreeNode
    """
    if len(inorder) == 0:
      return None
    # 前序遍历第一个值为根节点
    root = TreeNode(postorder[-1])
    # 查找根节点在中序遍历中的位置
    mid = 0
    while inorder[mid] != root.val:
      mid += 1
    # 构建左子树
    # 难点是前序数组的切分方法
    root.left = self.buildTree(inorder[:mid], postorder[:mid])
    # 构建右子树
    root.right = self.buildTree(inorder[mid+1:], postorder[mid:-1])
    return root