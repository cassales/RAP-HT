/*
 *    HoeffdingTree.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.classifiers.trees;

import com.github.javacliparser.*;
import com.yahoo.labs.samoa.instances.Instance;
import moa.AbstractMOAObject;
import moa.capabilities.CapabilitiesHandler;
import moa.capabilities.Capability;
import moa.capabilities.ImmutableCapabilities;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.bayes.NaiveBayes;
import moa.classifiers.core.AttributeSplitSuggestion;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.DiscreteAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NullAttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.NumericAttributeClassObserver;
import moa.classifiers.core.conditionaltests.InstanceConditionalTest;
import moa.classifiers.core.splitcriteria.SplitCriterion;
import moa.core.StringUtils;
import moa.core.*;
import moa.options.ClassOption;

import java.io.*;
import java.util.*;

/**
 * Hoeffding Tree or VFDT.
 *
 * A Hoeffding tree is an incremental, anytime decision tree induction algorithm
 * that is capable of learning from massive data streams, assuming that the
 * distribution generating examples does not change over time. Hoeffding trees
 * exploit the fact that a small sample can often be enough to choose an optimal
 * splitting attribute. This idea is supported mathematically by the Hoeffding
 * bound, which quantiﬁes the number of observations (in our case, examples)
 * needed to estimate some statistics within a prescribed precision (in our
 * case, the goodness of an attribute).</p> <p>A theoretically appealing feature
 * of Hoeffding Trees not shared by other incremental decision tree learners is
 * that it has sound guarantees of performance. Using the Hoeffding bound one
 * can show that its output is asymptotically nearly identical to that of a
 * non-incremental learner using inﬁnitely many examples. See for details:</p>
 *
 * <p>G. Hulten, L. Spencer, and P. Domingos. Mining time-changing data streams.
 * In KDD’01, pages 97–106, San Francisco, CA, 2001. ACM Press.</p>
 *
 * <p>Parameters:</p> <ul> <li> -m : Maximum memory consumed by the tree</li>
 * <li> -n : Numeric estimator to use : <ul> <li>Gaussian approximation
 * evaluating 10 splitpoints</li> <li>Gaussian approximation evaluating 100
 * splitpoints</li> <li>Greenwald-Khanna quantile summary with 10 tuples</li>
 * <li>Greenwald-Khanna quantile summary with 100 tuples</li>
 * <li>Greenwald-Khanna quantile summary with 1000 tuples</li> <li>VFML method
 * with 10 bins</li> <li>VFML method with 100 bins</li> <li>VFML method with
 * 1000 bins</li> <li>Exhaustive binary tree</li> </ul> </li> <li> -e : How many
 * instances between memory consumption checks</li> <li> -g : The number of
 * instances a leaf should observe between split attempts</li> <li> -s : Split
 * criterion to use. Example : InfoGainSplitCriterion</li> <li> -c : The
 * allowable error in split decision, values closer to 0 will take longer to
 * decide</li> <li> -t : Threshold below which a split will be forced to break
 * ties</li> <li> -b : Only allow binary splits</li> <li> -z : Stop growing as
 * soon as memory limit is hit</li> <li> -r : Disable poor attributes</li> <li>
 * -p : Disable pre-pruning</li> 
 *  <li> -l : Leaf prediction to use: MajorityClass (MC), Naive Bayes (NB) or NaiveBayes
 * adaptive (NBAdaptive).</li>
 *  <li> -q : The number of instances a leaf should observe before
 * permitting Naive Bayes</li>
 * </ul>
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class RAPHT extends AbstractClassifier implements MultiClassClassifier,
                                                                 CapabilitiesHandler {

    private static final long serialVersionUID = 1L;
    private int instancesSeen = 0;
    private int pruneInterval = 0;
    private int pruneThreshold = 0;
    private boolean LRUPrunningActive = false;
    private boolean subtreeRaisingActive = false;
    protected long evaluationTime = 0;

    @Override
    public String getPurposeString() {
        return "Hoeffding Tree or VFDT.";
    }

    public ClassOption numericEstimatorOption = new ClassOption("numericEstimator",
            'n', "Numeric estimator to use.", NumericAttributeClassObserver.class,
            "GaussianNumericAttributeClassObserver");

    public ClassOption nominalEstimatorOption = new ClassOption("nominalEstimator",
            'd', "Nominal estimator to use.", DiscreteAttributeClassObserver.class,
            "NominalAttributeClassObserver");

    public IntOption gracePeriodOption = new IntOption(
            "gracePeriod",
            'g',
            "The number of instances a leaf should observe between split attempts.",
            200, 0, Integer.MAX_VALUE);

    public ClassOption splitCriterionOption = new ClassOption("splitCriterion",
            's', "Split criterion to use.", SplitCriterion.class,
            "InfoGainSplitCriterion");

    public FloatOption splitConfidenceOption = new FloatOption(
            "splitConfidence",
            'c',
            "The allowable error in split decision, values closer to 0 will take longer to decide.",
            0.0000001, 0.0, 1.0);

    public FloatOption tieThresholdOption = new FloatOption("tieThreshold",
            't', "Threshold below which a split will be forced to break ties.",
            0.05, 0.0, 1.0);

    public FlagOption binarySplitsOption = new FlagOption("binarySplits", 'b',
        "Only allow binary splits.");

    public FlagOption outputBinSplitOption = new FlagOption("outputBinSplit", 'o',
            "Output the bin that the split has used.");

    public StringOption fileOutputBinSplitOption = new StringOption("fileOutputBinSplit", 'O',
            "Output the bin that the split has used.", "binused.txt");

    public FlagOption removePoorAttsOption = new FlagOption("removePoorAtts",
            'r', "Disable poor attributes.");

    public FlagOption noPrePruneOption = new FlagOption("noPrePrune", 'p',
            "Disable pre-pruning.");

    public FlagOption LRUPruneOption = new FlagOption("LRUPrune", 'u',
            "Enable LRU pruning.");

    public IntOption LRUThresholdOption = new IntOption("LRUThreshold",
            'T',
            "The assumed i.i.d. percentage threshold. Should prune intervals lower than the one created with this.",
            5, 1, Integer.MAX_VALUE);

    public IntOption PruneWindowOption = new IntOption("PruneWindow",
            'W',
            "Defines the interval that we check for pruning opportunities.",
            2000, 1, Integer.MAX_VALUE);

    public FlagOption subtreeRaisingOption = new FlagOption("subtreeRaising", 'R',
            "enable subtree-raising when pruning");

    public static class FoundNode {

        public Node node;

        public SplitNode parent;

        public int parentBranch;

        public FoundNode(Node node, SplitNode parent, int parentBranch) {
            this.node = node;
            this.parent = parent;
            this.parentBranch = parentBranch;
        }
    }

    public static class Node extends AbstractMOAObject {

        private static final long serialVersionUID = 1L;

        protected DoubleVector observedClassDistribution;

        protected int lastAccess = -1;

        public Node(double[] classObservations) {
            this.observedClassDistribution = new DoubleVector(classObservations);
        }

        public int calcByteSize() {
            return (int) (SizeOf.sizeOf(this) + SizeOf.fullSizeOf(this.observedClassDistribution));
        }

        public int calcByteSizeIncludingSubtree() {
            return calcByteSize();
        }

        public boolean isLeaf() {
            return true;
        }

        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                int parentBranch) {
            return new FoundNode(this, parent, parentBranch);
        }

        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                                              int parentBranch, int TimeStamp) {
            return new FoundNode(this, parent, parentBranch);
        }

        public double[] getObservedClassDistribution() {
            return this.observedClassDistribution.getArrayCopy();
        }

        public double[] getClassVotes(Instance inst, RAPHT ht) {
            return this.observedClassDistribution.getArrayCopy();
        }

        public boolean observedClassDistributionIsPure() {
            return this.observedClassDistribution.numNonZeroEntries() < 2;
        }

        public void describeSubtree(RAPHT ht, StringBuilder out,
                                    int indent) {
            StringUtils.appendIndented(out, indent, "Leaf ");
            out.append(ht.getClassNameString());
            out.append(" = ");
            out.append(ht.getClassLabelString(this.observedClassDistribution.maxIndex()));
            out.append(" weights: ");
            this.observedClassDistribution.getSingleLineDescription(out,
                    ht.treeRoot.observedClassDistribution.numValues());
            StringUtils.appendNewline(out);
        }

        public int subtreeDepth() {
            return 0;
        }

        public double calculatePromise() {
            double totalSeen = this.observedClassDistribution.sumOfValues();
            return totalSeen > 0.0 ? (totalSeen - this.observedClassDistribution.getValue(this.observedClassDistribution.maxIndex()))
                    : 0.0;
        }

        @Override
        public void getDescription(StringBuilder sb, int indent) {
            describeSubtree(null, sb, indent);
        }
    }

    public static class SplitNode extends Node {

        private static final long serialVersionUID = 1L;

        protected InstanceConditionalTest splitTest;

        protected AutoExpandVector<Node> children;

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.sizeOf(this.children) + SizeOf.fullSizeOf(this.splitTest));
        }

        @Override
        public int calcByteSizeIncludingSubtree() {
            int byteSize = calcByteSize();
            for (Node child : this.children) {
                if (child != null) {
                    byteSize += child.calcByteSizeIncludingSubtree();
                }
            }
            return byteSize;
        }

        public SplitNode(InstanceConditionalTest splitTest,
                double[] classObservations, int size) {
            super(classObservations);
            this.splitTest = splitTest;
            this.children = new AutoExpandVector<>(size);
        }
        
        public SplitNode(InstanceConditionalTest splitTest,
                double[] classObservations) {
            super(classObservations);
            this.splitTest = splitTest;
            this.children = new AutoExpandVector<>();
        }


        public int numChildren() {
            return this.children.size();
        }

        public void setChild(int index, Node child) {
            if ((this.splitTest.maxBranches() >= 0)
                    && (index >= this.splitTest.maxBranches())) {
                throw new IndexOutOfBoundsException();
            }
            this.children.set(index, child);
        }

        public Node getChild(int index) {
            return this.children.get(index);
        }

        public int instanceChildIndex(Instance inst) {
            return this.splitTest.branchForInstance(inst);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                int parentBranch) {
            int childIndex = instanceChildIndex(inst);
            if (childIndex >= 0) {
                Node child = getChild(childIndex);
                if (child != null) {
                    return child.filterInstanceToLeaf(inst, this, childIndex);
                }
                return new FoundNode(null, this, childIndex);
            }
            return new FoundNode(this, parent, parentBranch);
        }

        @Override
        public FoundNode filterInstanceToLeaf(Instance inst, SplitNode parent,
                                              int parentBranch, int TimeStamp) {
            int childIndex = instanceChildIndex(inst);
            this.lastAccess = TimeStamp;
            if (childIndex >= 0) {
                Node child = getChild(childIndex);
                if (child != null) {
                    return child.filterInstanceToLeaf(inst, this, childIndex, TimeStamp);
                }
                return new FoundNode(null, this, childIndex);
            }
            return new FoundNode(this, parent, parentBranch);
        }

        @Override
        public void describeSubtree(RAPHT ht, StringBuilder out,
                                    int indent) {
            for (int branch = 0; branch < numChildren(); branch++) {
                Node child = getChild(branch);
                if (child != null) {
                    StringUtils.appendIndented(out, indent, "if ");
                    out.append(this.splitTest.describeConditionForBranch(branch,
                            ht.getModelContext()));
                    out.append(": ");
                    StringUtils.appendNewline(out);
                    child.describeSubtree(ht, out, indent + 2);
                }
            }
        }

        @Override
        public int subtreeDepth() {
            int maxChildDepth = 0;
            for (Node child : this.children) {
                if (child != null) {
                    int depth = child.subtreeDepth();
                    if (depth > maxChildDepth) {
                        maxChildDepth = depth;
                    }
                }
            }
            return maxChildDepth + 1;
        }
    }

    public static abstract class LearningNode extends Node {

        private static final long serialVersionUID = 1L;

        public LearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        public abstract void learnFromInstance(Instance inst, RAPHT ht);
    }

    public static class InactiveLearningNode extends LearningNode {

        private static final long serialVersionUID = 1L;

        public InactiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public void learnFromInstance(Instance inst, RAPHT ht) {
            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
        }
    }

    public static class ActiveLearningNode extends LearningNode {

        private static final long serialVersionUID = 1L;

        protected double weightSeenAtLastSplitEvaluation;

        protected AutoExpandVector<AttributeClassObserver> attributeObservers = new AutoExpandVector<>();
        
        protected boolean isInitialized;

        public ActiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
            this.weightSeenAtLastSplitEvaluation = getWeightSeen();
            this.isInitialized = false;
        }

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.fullSizeOf(this.attributeObservers));
        }

        @Override
        public void learnFromInstance(Instance inst, RAPHT ht) {
            this.lastAccess = ht.instancesSeen;
            if (!this.isInitialized) {
                this.attributeObservers = new AutoExpandVector<>(inst.numAttributes());
                this.isInitialized = true;
            }

            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
            for (int i = 0; i < inst.numAttributes() - 1; i++) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs == null) {
                    obs = inst.attribute(instAttIndex).isNominal() ? ht.newNominalClassObserver() : ht.newNumericClassObserver();
                    this.attributeObservers.set(i, obs);
                }
                obs.observeAttributeClass(inst.value(instAttIndex), (int) inst.classValue(), inst.weight());
            }
        }

        public double getWeightSeen() {
            return this.observedClassDistribution.sumOfValues();
        }

        public double getWeightSeenAtLastSplitEvaluation() {
            return this.weightSeenAtLastSplitEvaluation;
        }

        public void setWeightSeenAtLastSplitEvaluation(double weight) {
            this.weightSeenAtLastSplitEvaluation = weight;
        }

        public AttributeSplitSuggestion[] getBestSplitSuggestions(
                SplitCriterion criterion, RAPHT ht) {
            List<AttributeSplitSuggestion> bestSuggestions = new LinkedList<>();
            double[] preSplitDist = this.observedClassDistribution.getArrayCopy();
            if (!ht.noPrePruneOption.isSet()) {
                // add null split as an option
                bestSuggestions.add(new AttributeSplitSuggestion(null,
                        new double[0][], criterion.getMeritOfSplit(
                        preSplitDist,
                        new double[][]{preSplitDist})));
            }
            for (int i = 0; i < this.attributeObservers.size(); i++) {
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs != null) {
                    AttributeSplitSuggestion bestSuggestion = obs.getBestEvaluatedSplitSuggestion(criterion,
                            preSplitDist, i, ht.binarySplitsOption.isSet());
                    if (bestSuggestion != null) {
                        bestSuggestions.add(bestSuggestion);
                    }
                }
            }
            return bestSuggestions.toArray(new AttributeSplitSuggestion[bestSuggestions.size()]);
        }

        public void disableAttribute(int attIndex) {
            this.attributeObservers.set(attIndex,
                    new NullAttributeClassObserver());
        }
    }

    protected Node treeRoot;

    protected int decisionNodeCount;

    protected int activeLeafNodeCount;

    protected int inactiveLeafNodeCount;

    protected double inactiveLeafByteSizeEstimate;

    protected double activeLeafByteSizeEstimate;

    protected double byteSizeEstimateOverheadFraction;

    protected boolean growthAllowed;

    protected int numBins;

    protected String numbinOutputFile;

    protected int learnPruned = 0;
    protected int splitPruned = 0;

    protected float averageNodeSize = 0;
    protected int averageCount = 0;

    protected int maxNodeSize = 0;

    public int calcByteSize() {
        int size = (int) SizeOf.sizeOf(this);
        if (this.treeRoot != null) {
            size += this.treeRoot.calcByteSizeIncludingSubtree();
        }
        return size;
    }

    @Override
    public int measureByteSize() {
        return calcByteSize();
    }

    @Override
    public void resetLearningImpl() {
        this.treeRoot = null;
        this.decisionNodeCount = 0;
        this.activeLeafNodeCount = 0;
        this.inactiveLeafNodeCount = 0;
        this.inactiveLeafByteSizeEstimate = 0.0;
        this.activeLeafByteSizeEstimate = 0.0;
        this.byteSizeEstimateOverheadFraction = 1.0;
        this.instancesSeen = 0;
        this.learnPruned = 0;
        this.splitPruned = 0;
        this.LRUPrunningActive = this.LRUPruneOption.isSet();
        if (this.LRUPrunningActive) {
            this.pruneInterval = this.PruneWindowOption.getValue();
            this.pruneThreshold = this.LRUThresholdOption.getValue();
        }
        this.subtreeRaisingActive = this.subtreeRaisingOption.isSet();
        this.growthAllowed = true;
        if (this.outputBinSplitOption.isSet()) {
            if (this.numericEstimatorOption.getValueAsCLIString().indexOf("-n") == -1)
                this.numBins = 10;
            else
                this.numBins = Integer.parseInt(this.numericEstimatorOption.getValueAsCLIString().split("-n ")[1]);
            this.numbinOutputFile = this.fileOutputBinSplitOption.getValue();
        }
        if (this.leafpredictionOption.getChosenIndex()>0) { 
            this.removePoorAttsOption = null;
        }
    }

    public double getUsageFactor(int depth) {
        return 1 + Math.log(Math.pow(2,depth/2.0));
    }

    public double get2AtDepth(int depth) {
        return Math.pow(2,depth);
    }
    public double getWorstExpectedInterval(int depth) {
        return Math.ceil(get2AtDepth(depth) / (this.pruneThreshold/100.0));
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        this.instancesSeen++;
        if (this.treeRoot == null) {
            if (this.LRUPrunningActive) {
                this.pruneThreshold = this.LRUThresholdOption.getValue();
                this.pruneInterval = this.PruneWindowOption.getValue();
            }
            this.subtreeRaisingActive = this.subtreeRaisingOption.isSet();
            this.treeRoot = newLearningNode();
            this.activeLeafNodeCount = 1;
        }
        FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst, null, -1, this.instancesSeen);
        Node leafNode = foundNode.node;
        if (leafNode == null) {
            leafNode = newLearningNode();
            foundNode.parent.setChild(foundNode.parentBranch, leafNode);
            this.activeLeafNodeCount++;
        }
        if (leafNode instanceof LearningNode) {
            LearningNode learningNode = (LearningNode) leafNode;
            learningNode.learnFromInstance(inst, this);
            if (this.growthAllowed
                    && (learningNode instanceof ActiveLearningNode)) {
                ActiveLearningNode activeLearningNode = (ActiveLearningNode) learningNode;
                double weightSeen = activeLearningNode.getWeightSeen();
                if (weightSeen
                        - activeLearningNode.getWeightSeenAtLastSplitEvaluation() >= this.gracePeriodOption.getValue()) {
                    attemptToSplit(activeLearningNode, foundNode.parent,
                            foundNode.parentBranch);
                    activeLearningNode.setWeightSeenAtLastSplitEvaluation(weightSeen);
                }
            }
        }

        if(this.LRUPrunningActive && this.instancesSeen % this.pruneInterval == 0) {
            long begin = TimingUtils.getNanoCPUTimeOfCurrentThread();
            this.checkTreeUsage(this.treeRoot, this.instancesSeen);
            this.evaluationTime += TimingUtils.getNanoCPUTimeOfCurrentThread() - begin;
        }
    }

    private int checkTreeUsage(Node node, int ts) {
        int depth = getNodeDepth(node);
        int interval = ts - node.lastAccess;
        double wThresh = this.getWorstExpectedInterval(depth);
        if (interval >= wThresh) {
            return 1; // we detect a node should be pruned, we inform the parent to prune, but we need to prune from the grandParent
        }
        int shouldPrune = -1;
        if (node instanceof SplitNode) {
            for (int i = 0; i < ((SplitNode) node).children.size(); i++) {
                Node n = ((SplitNode) node).getChild(i);
                if (n != null)
                    shouldPrune = checkTreeUsage(n, ts);
                if (shouldPrune == 1 && node != this.treeRoot) {
                    //we still need to move up the tree
                    return 0;
                } else if (shouldPrune == 0) {
                    if (this.subtreeRaisingActive) {
                        // We are in the grandparent and using subtree raising.
                        // wThresh = Math.ceil(this.pruneThreshold * this.getUsageFactor(depth+1));
                        wThresh = this.getWorstExpectedInterval(depth);
                        boolean majorityTest = (ts - ((SplitNode) n).getChild(0).lastAccess) < wThresh;
                        int idMinor = majorityTest ? 1 : 0;
                        Node maj = ((SplitNode) n).children.get(1 - idMinor);
                        // Promote majority to the parent's place
                        ((SplitNode) node).setChild(i,maj);
                        // Finally, update counters
                        this.updateCounterForNodeRemoval(((SplitNode) n).children.get(idMinor));
                    } else {
                        // we are in the grandparent without subtree raising
                        // checking class just for sanity, there should not be a case where something other than split node has a child
                        if (n instanceof SplitNode)
                            ((SplitNode) node).setChild(i, createLearningAndUpdateCounters(n));
                    }
                    int ttnodes = this.inactiveLeafNodeCount + this.decisionNodeCount + this.activeLeafNodeCount;
                    if (ttnodes > this.maxNodeSize)
                        this.maxNodeSize = ttnodes;
                    this.averageCount++;
                    this.averageNodeSize = this.averageNodeSize + (ttnodes - averageNodeSize)/this.averageCount;
                    return -1;
                } else if (shouldPrune == 1 && node == this.treeRoot) {
                    //we are at the root, but one of its children needs pruning
                    if (this.subtreeRaisingActive) {
                        // We are in the parent (which is the root) and using subtree raising.
                        // We already know the rarely accessed node (n)
                        int indexMinor = ((SplitNode) node).children.indexOf(n);
                        Node minor = ((SplitNode) node).getChild(indexMinor);
                        Node maj = ((SplitNode) node).getChild(1-indexMinor);
                        // Update root
                        this.treeRoot = maj;
                        this.updateCounterForNodeRemoval(minor);
                    } else {
                        // forget everything pruning
                        this.treeRoot = createLearningAndUpdateCounters(this.treeRoot);
                    }
                    int ttnodes = this.inactiveLeafNodeCount + this.decisionNodeCount + this.activeLeafNodeCount;
                    if (ttnodes > this.maxNodeSize)
                        this.maxNodeSize = ttnodes;
                    this.averageCount++;
                    this.averageNodeSize = this.averageNodeSize + (ttnodes - averageNodeSize)/this.averageCount;
                    return -1;
                } // no else needed as shouldPrune == -1 is the standard (no pruning needed)
            }
        }
        return shouldPrune;
    }

    protected void updateCounterForNodeRemoval(Node minor) {
        int split = this.subtreeRaisingActive? (this.countSplitInChildren(minor) + 1) : this.countSplitInChildren(minor);
        int learn = this.countLearnInChildren(minor);
        this.splitPruned += split;
        this.learnPruned += learn;
        this.activeLeafNodeCount -= learn;
        this.decisionNodeCount -= split;
    }

    protected Node createLearningAndUpdateCounters(Node n) {
        Node newChild = newLearningNode();
        newChild.lastAccess = this.instancesSeen;
        this.updateCounterForNodeRemoval(n);
        this.activeLeafNodeCount++;
        return newChild;
    }

    private int countSplitInChildren(Node n) {
        int ret = 0;
        if (n instanceof SplitNode) {
            ret++;
            for (Node n2 : ((SplitNode) n).children) {
                ret += countSplitInChildren(n2);
            }
        }
        return ret;
    }

    private int countLearnInChildren(Node n) {
        int ret = 0;
        if (n instanceof LearningNode) {
            ret++;
        } else if (n instanceof SplitNode) {
            for (Node n2 : ((SplitNode) n).children) {
                ret += countLearnInChildren(n2);
            }
        }
        return ret;
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        if (this.treeRoot != null) {
            FoundNode foundNode = this.treeRoot.filterInstanceToLeaf(inst,
                    null, -1);
            Node leafNode = foundNode.node;
            if (leafNode == null) {
                leafNode = foundNode.parent;
            }
            return leafNode.getClassVotes(inst, this);
          } else {
            int numClasses = inst.dataset().numClasses();
            return new double[numClasses];
          }
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[]{
                    new Measurement("evaluationTime",
                    TimingUtils.nanoTimeToSeconds(this.evaluationTime)),
                    new Measurement("tree size (nodes)", this.decisionNodeCount
                    + this.activeLeafNodeCount + this.inactiveLeafNodeCount),
                    new Measurement("tree size (leaves)", this.activeLeafNodeCount
                    + this.inactiveLeafNodeCount),
                    new Measurement("active learning leaves",
                    this.activeLeafNodeCount),
                    new Measurement("tree depth", measureTreeDepth()),
                    new Measurement("active leaf byte size estimate",
                    this.activeLeafByteSizeEstimate),
                    new Measurement("inactive leaf byte size estimate",
                    this.inactiveLeafByteSizeEstimate),
                    new Measurement("byte size estimate overhead",
                    this.byteSizeEstimateOverheadFraction),
                    new Measurement("INTERVAL",
                    this.pruneInterval),
                    new Measurement("THRESHOLD (depth 0)",
                    this.pruneThreshold),
                    new Measurement("PRUNED", this.learnPruned +
                    this.splitPruned),
                    new Measurement("PRUNE-SPLIT",
                    this.splitPruned),
                    new Measurement("PRUNE-LEARN",
                    this.learnPruned),
                    new Measurement("AVG-NODESIZE",
                    this.averageNodeSize),
                    new Measurement("MAX-NODESIZE",
                    this.maxNodeSize)};
    }

    public int measureTreeDepth() {
        if (this.treeRoot != null) {
            return this.treeRoot.subtreeDepth();
        }
        return 0;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        this.treeRoot.describeSubtree(this, out, indent);
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }

    public static double computeHoeffdingBound(double range, double confidence,
            double n) {
        return Math.sqrt(((range * range) * Math.log(1.0 / confidence))
                / (2.0 * n));
    }

    //Procedure added for Hoeffding Adaptive Trees (ADWIN)
    protected SplitNode newSplitNode(InstanceConditionalTest splitTest,
            double[] classObservations, int size) {
        return new SplitNode(splitTest, classObservations, size);
    }

    protected SplitNode newSplitNode(InstanceConditionalTest splitTest,
            double[] classObservations) {
        return new SplitNode(splitTest, classObservations);
    }


    protected AttributeClassObserver newNominalClassObserver() {
        AttributeClassObserver nominalClassObserver = (AttributeClassObserver) getPreparedClassOption(this.nominalEstimatorOption);
        return (AttributeClassObserver) nominalClassObserver.copy();
    }

    protected AttributeClassObserver newNumericClassObserver() {
        AttributeClassObserver numericClassObserver = (AttributeClassObserver) getPreparedClassOption(this.numericEstimatorOption);
        return (AttributeClassObserver) numericClassObserver.copy();
    }

    protected void attemptToSplit(ActiveLearningNode node, SplitNode parent,
            int parentIndex) {
        if (!node.observedClassDistributionIsPure()) {
            SplitCriterion splitCriterion = (SplitCriterion) getPreparedClassOption(this.splitCriterionOption);
            AttributeSplitSuggestion[] bestSplitSuggestions = node.getBestSplitSuggestions(splitCriterion, this);
            Arrays.sort(bestSplitSuggestions);
            boolean shouldSplit = false;
            if (bestSplitSuggestions.length < 2) {
                shouldSplit = bestSplitSuggestions.length > 0;
            } else {
                double hoeffdingBound = computeHoeffdingBound(splitCriterion.getRangeOfMerit(node.getObservedClassDistribution()),
                        this.splitConfidenceOption.getValue(), node.getWeightSeen());
                AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                AttributeSplitSuggestion secondBestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 2];
                if ((bestSuggestion.merit - secondBestSuggestion.merit > hoeffdingBound)
                        || (hoeffdingBound < this.tieThresholdOption.getValue())) {
                    shouldSplit = true;
                }
                // }
                if ((this.removePoorAttsOption != null)
                        && this.removePoorAttsOption.isSet()) {
                    Set<Integer> poorAtts = new HashSet<Integer>();
                    // scan 1 - add any poor to set
                    for (int i = 0; i < bestSplitSuggestions.length; i++) {
                        if (bestSplitSuggestions[i].splitTest != null) {
                            int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                            if (splitAtts.length == 1) {
                                if (bestSuggestion.merit
                                        - bestSplitSuggestions[i].merit > hoeffdingBound) {
                                    poorAtts.add(new Integer(splitAtts[0]));
                                }
                            }
                        }
                    }
                    // scan 2 - remove good ones from set
                    for (int i = 0; i < bestSplitSuggestions.length; i++) {
                        if (bestSplitSuggestions[i].splitTest != null) {
                            int[] splitAtts = bestSplitSuggestions[i].splitTest.getAttsTestDependsOn();
                            if (splitAtts.length == 1) {
                                if (bestSuggestion.merit
                                        - bestSplitSuggestions[i].merit < hoeffdingBound) {
                                    poorAtts.remove(new Integer(splitAtts[0]));
                                }
                            }
                        }
                    }
                    for (int poorAtt : poorAtts) {
                        node.disableAttribute(poorAtt);
                    }
                }
            }
            if (shouldSplit) {
                AttributeSplitSuggestion splitDecision = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                if (splitDecision.splitTest == null) {
                    // preprune - null wins
                    deactivateLearningNode(node, parent, parentIndex);
                } else {
                    // will split
                    if (this.outputBinSplitOption.isSet()) {
                        // get the bin
                        String data = splitDecision.binIndex + "\n";
                        OutputStream os = null;
                        try {
                            os = new FileOutputStream(new File(this.numbinOutputFile),true);
                            os.write(data.getBytes(), 0, data.length());
                            os.close();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    SplitNode newSplit = newSplitNode(splitDecision.splitTest,
                            node.getObservedClassDistribution(),splitDecision.numSplits() );
                    for (int i = 0; i < splitDecision.numSplits(); i++) {
                        Node newChild = newLearningNode(splitDecision.resultingClassDistributionFromSplit(i));
                        newChild.lastAccess = this.instancesSeen;
                        newSplit.setChild(i, newChild);
                    }
                    this.activeLeafNodeCount--;
                    this.decisionNodeCount++;
                    this.activeLeafNodeCount += splitDecision.numSplits();
                    int tmpacc;
                    if (parent == null) {
                        tmpacc = this.treeRoot.lastAccess;
                        this.treeRoot = newSplit;
                    } else {
                        tmpacc = parent.lastAccess;
                        parent.setChild(parentIndex, newSplit);
                    }
                    newSplit.lastAccess = tmpacc;
                    // get tree nodes
                    int ttnodes = this.inactiveLeafNodeCount + this.decisionNodeCount + this.activeLeafNodeCount;
                    if (ttnodes > this.maxNodeSize)
                        this.maxNodeSize = ttnodes;
                    this.averageCount++;
                    this.averageNodeSize = this.averageNodeSize + (ttnodes - averageNodeSize)/this.averageCount;

                }
            }
        }
    }



    protected void deactivateLearningNode(ActiveLearningNode toDeactivate,
            SplitNode parent, int parentBranch) {
        Node newLeaf = new InactiveLearningNode(toDeactivate.getObservedClassDistribution());
        if (parent == null) {
            this.treeRoot = newLeaf;
        } else {
            parent.setChild(parentBranch, newLeaf);
        }
        this.activeLeafNodeCount--;
        this.inactiveLeafNodeCount++;
    }

    protected FoundNode[] findLearningNodes() {
        List<FoundNode> foundList = new LinkedList<FoundNode>();
        findLearningNodes(this.treeRoot, null, -1, foundList);
        return foundList.toArray(new FoundNode[foundList.size()]);
    }

    protected void findLearningNodes(Node node, SplitNode parent,
            int parentBranch, List<FoundNode> found) {
        if (node != null) {
            if (node instanceof LearningNode) {
                found.add(new FoundNode(node, parent, parentBranch));
            }
            if (node instanceof SplitNode) {
                SplitNode splitNode = (SplitNode) node;
                for (int i = 0; i < splitNode.numChildren(); i++) {
                    findLearningNodes(splitNode.getChild(i), splitNode, i,
                            found);
                }
            }
        }
    }

    public MultiChoiceOption leafpredictionOption = new MultiChoiceOption(
            "leafprediction", 'l', "Leaf prediction to use.", new String[]{
                "MC", "NB", "NBAdaptive"}, new String[]{
                "Majority class",
                "Naive Bayes",
                "Naive Bayes Adaptive"}, 2);

    public IntOption nbThresholdOption = new IntOption(
            "nbThreshold",
            'q',
            "The number of instances a leaf should observe before permitting Naive Bayes.",
            0, 0, Integer.MAX_VALUE);

    public static class LearningNodeNB extends ActiveLearningNode {

        private static final long serialVersionUID = 1L;

        public LearningNodeNB(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public double[] getClassVotes(Instance inst, RAPHT ht) {
            if (getWeightSeen() >= ht.nbThresholdOption.getValue()) {
                return NaiveBayes.doNaiveBayesPrediction(inst,
                        this.observedClassDistribution,
                        this.attributeObservers);
            }
            return super.getClassVotes(inst, ht);
        }

        @Override
        public void disableAttribute(int attIndex) {
            // should not disable poor atts - they are used in NB calc
        }
    }

    public static class LearningNodeNBAdaptive extends LearningNodeNB {

        private static final long serialVersionUID = 1L;

        protected double mcCorrectWeight = 0.0;

        protected double nbCorrectWeight = 0.0;

        public LearningNodeNBAdaptive(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public void learnFromInstance(Instance inst, RAPHT ht) {
            int trueClass = (int) inst.classValue();
            if (this.observedClassDistribution.maxIndex() == trueClass) {
                this.mcCorrectWeight += inst.weight();
            }
            if (Utils.maxIndex(NaiveBayes.doNaiveBayesPrediction(inst,
                    this.observedClassDistribution, this.attributeObservers)) == trueClass) {
                this.nbCorrectWeight += inst.weight();
            }
            super.learnFromInstance(inst, ht);
        }

        @Override
        public double[] getClassVotes(Instance inst, RAPHT ht) {
            if (this.mcCorrectWeight > this.nbCorrectWeight) {
                return this.observedClassDistribution.getArrayCopy();
            }
            return NaiveBayes.doNaiveBayesPrediction(inst,
                    this.observedClassDistribution, this.attributeObservers);
        }
    }

    public int getNodeDepth(Node wanted) {
        if (wanted == this.treeRoot)
            return 0;
        else
            return getNodeSubTreeDepth(treeRoot, wanted);
    }

    public int getNodeSubTreeDepth(Node subTreeRoot, Node wanted) {
        ArrayList<Node> l = new ArrayList<>();
//        I believe this is not going to be used...
        if (subTreeRoot instanceof LearningNode)
            return 0;
        else if (subTreeRoot != null) {
            for (Node c : ((SplitNode) subTreeRoot).children) {
                if (c == wanted) {
                    return 1;
                } else {
                    l.add(c);
                }
            }
            // none of the children is the wanted node
            // check the grandchildren of subtreeroot
            for (Node c : l) {
                int aux = this.getNodeSubTreeDepth(c, wanted);
                if (aux > 0)
                    return 1 + aux;
            }
        }
        return -100;
    }

    protected LearningNode newLearningNode() {
        return newLearningNode(new double[0]);
    }

    protected LearningNode newLearningNode(double[] initialClassObservations) {
        LearningNode ret;
        int predictionOption = this.leafpredictionOption.getChosenIndex();
        if (predictionOption == 0) { //MC
            ret = new ActiveLearningNode(initialClassObservations);
        } else if (predictionOption == 1) { //NB
            ret = new LearningNodeNB(initialClassObservations);
        } else { //NBAdaptive
            ret = new LearningNodeNBAdaptive(initialClassObservations);
        }
        return ret;
    }

    @Override
    public ImmutableCapabilities defineImmutableCapabilities() {
      if (this.getClass() == RAPHT.class)
        return new ImmutableCapabilities(Capability.VIEW_STANDARD, Capability.VIEW_LITE);
      else
        return new ImmutableCapabilities(Capability.VIEW_STANDARD);
    }
}
