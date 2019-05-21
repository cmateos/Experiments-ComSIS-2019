// 
// Decompiled by Procyon v0.5.30
// 

package weka.core;

import weka.core.converters.ConverterUtils;
import java.io.FileReader;
import java.util.Map;
import weka.experiment.Stats;
import java.util.Random;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;
import java.io.IOException;
import weka.core.converters.ArffLoader;
import java.io.Reader;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.AbstractList;

public class Instances extends AbstractList<Instance> implements Serializable, RevisionHandler
{
    static final long serialVersionUID = -19412345060742748L;
    public static final String FILE_EXTENSION = ".arff";
    public static final String SERIALIZED_OBJ_FILE_EXTENSION = ".bsi";
    public static final String ARFF_RELATION = "@relation";
    public static final String ARFF_DATA = "@data";
    protected String m_RelationName;
    public ArrayList<Attribute> m_Attributes;
    protected HashMap<String, Integer> m_NamesToAttributeIndices;
    public ArrayList<Instance> m_Instances;
    public int m_ClassIndex;
    protected int m_Lines;
    
    public Instances(final Reader reader) throws IOException {
        this.m_Lines = 0;
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, 1000, false);
        this.initialize(arff.getData(), 1000);
        arff.setRetainStringValues(true);
        Instance inst;
        while ((inst = arff.readInstance(this)) != null) {
            this.m_Instances.add(inst);
        }
        this.compactify();
    }
    
    public Instances(final Reader reader, final int capacity) throws IOException {
        this.m_Lines = 0;
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, 0);
        final Instances header = arff.getStructure();
        this.initialize(header, capacity);
        this.m_Lines = arff.getLineNo();
    }
    
    public Instances(final Instances dataset) {
        this.m_Lines = 0;
        this.m_ClassIndex = dataset.m_ClassIndex;
        this.m_RelationName = dataset.m_RelationName;
        this.m_Attributes = dataset.m_Attributes;
        this.m_NamesToAttributeIndices = dataset.m_NamesToAttributeIndices;
        this.m_Instances = new ArrayList<Instance>(dataset.m_Instances);
    }
    
    public Instances(final Instances dataset, final int capacity) {
        this.m_Lines = 0;
        this.initialize(dataset, capacity);
    }
    
    protected void initialize(final Instances dataset, int capacity) {
        if (capacity < 0) {
            capacity = 0;
        }
        this.m_ClassIndex = dataset.m_ClassIndex;
        this.m_RelationName = dataset.m_RelationName;
        this.m_Attributes = dataset.m_Attributes;
        this.m_NamesToAttributeIndices = dataset.m_NamesToAttributeIndices;
        this.m_Instances = new ArrayList<Instance>(capacity);
    }
    
    public Instances(final Instances source, final int first, final int toCopy) {
        this(source, toCopy);
        if (first < 0 || first + toCopy > source.numInstances()) {
            throw new IllegalArgumentException("Parameters first and/or toCopy out of range");
        }
        source.copyInstances(first, this, toCopy);
    }
    
    public Instances(final String name, final ArrayList<Attribute> attInfo, final int capacity) {
        this.m_Lines = 0;
        final HashSet<String> names = new HashSet<String>();
        final StringBuffer nonUniqueNames = new StringBuffer();
        for (final Attribute att : attInfo) {
            if (names.contains(att.name())) {
                nonUniqueNames.append("'" + att.name() + "' ");
            }
            names.add(att.name());
        }
        if (names.size() != attInfo.size()) {
            throw new IllegalArgumentException("Attribute names are not unique! Causes: " + nonUniqueNames.toString());
        }
        names.clear();
        this.m_RelationName = name;
        this.m_ClassIndex = -1;
        this.m_Attributes = attInfo;
        this.m_NamesToAttributeIndices = new HashMap<String, Integer>((int)(this.numAttributes() / 0.75));
        for (int i = 0; i < this.numAttributes(); ++i) {
            this.attribute(i).setIndex(i);
            this.m_NamesToAttributeIndices.put(this.attribute(i).name(), i);
        }
        this.m_Instances = new ArrayList<Instance>(capacity);
    }
    
    public Instances stringFreeStructure() {
        final ArrayList<Attribute> newAtts = new ArrayList<Attribute>();
        for (final Attribute att : this.m_Attributes) {
            if (att.type() == 2) {
                newAtts.add(new Attribute(att.name(), (List<String>)null, att.index()));
            }
            else {
                if (att.type() != 4) {
                    continue;
                }
                newAtts.add(new Attribute(att.name(), new Instances(att.relation(), 0), att.index()));
            }
        }
        if (newAtts.size() == 0) {
            return new Instances(this, 0);
        }
        final ArrayList<Attribute> atts = Utils.cast(this.m_Attributes.clone());
        for (final Attribute att2 : newAtts) {
            atts.set(att2.index(), att2);
        }
        final Instances result = new Instances(this, 0);
        result.m_Attributes = atts;
        return result;
    }
    
    @Override
    public boolean add(final Instance instance) {
        final Instance newInstance = (Instance)instance.copy();
        newInstance.setDataset(this);
        this.m_Instances.add(newInstance);
        return true;
    }
    
    @Override
    public void add(final int index, final Instance instance) {
        final Instance newInstance = (Instance)instance.copy();
        newInstance.setDataset(this);
        this.m_Instances.add(index, newInstance);
    }
    
    public Attribute attribute(final int index) {
        return this.m_Attributes.get(index);
    }
    
    public Attribute attribute(final String name) {
        final Integer index = this.m_NamesToAttributeIndices.get(name);
        if (index != null) {
            return this.attribute(index);
        }
        return null;
    }
    
    public boolean checkForAttributeType(final int attType) {
        int i = 0;
        while (i < this.m_Attributes.size()) {
            if (this.attribute(i++).type() == attType) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkForStringAttributes() {
        return this.checkForAttributeType(2);
    }
    
    public boolean checkInstance(final Instance instance) {
        if (instance.numAttributes() != this.numAttributes()) {
            return false;
        }
        for (int i = 0; i < this.numAttributes(); ++i) {
            if (!instance.isMissing(i)) {
                if (this.attribute(i).isNominal() || this.attribute(i).isString()) {
                    if (!Utils.eq(instance.value(i), (int)instance.value(i))) {
                        return false;
                    }
                    if (Utils.sm(instance.value(i), 0.0) || Utils.gr(instance.value(i), this.attribute(i).numValues())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public Attribute classAttribute() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        return this.attribute(this.m_ClassIndex);
    }
    
    public int classIndex() {
        return this.m_ClassIndex;
    }
    
    public void compactify() {
        this.m_Instances.trimToSize();
    }
    
    public void delete() {
        this.m_Instances = new ArrayList<Instance>();
    }
    
    public void delete(final int index) {
        this.m_Instances.remove(index);
    }
    
    public void deleteAttributeAt(final int position) {
        if (position < 0 || position >= this.m_Attributes.size()) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (position == this.m_ClassIndex) {
            throw new IllegalArgumentException("Can't delete class attribute");
        }
        final ArrayList<Attribute> newList = new ArrayList<Attribute>(this.m_Attributes.size() - 1);
        final HashMap<String, Integer> newMap = new HashMap<String, Integer>((int)((this.m_Attributes.size() - 1) / 0.75));
        for (int i = 0; i < position; ++i) {
            final Attribute att = this.m_Attributes.get(i);
            newList.add(att);
            newMap.put(att.name(), i);
        }
        for (int i = position + 1; i < this.m_Attributes.size(); ++i) {
            final Attribute newAtt = (Attribute)this.m_Attributes.get(i).copy();
            newAtt.setIndex(i - 1);
            newList.add(newAtt);
            newMap.put(newAtt.name(), i - 1);
        }
        this.m_Attributes = newList;
        this.m_NamesToAttributeIndices = newMap;
        if (this.m_ClassIndex > position) {
            --this.m_ClassIndex;
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            this.instance(i).setDataset(null);
            this.instance(i).deleteAttributeAt(position);
            this.instance(i).setDataset(this);
        }
    }
    
    public void deleteAttributeType(final int attType) {
        int i = 0;
        while (i < this.m_Attributes.size()) {
            if (this.attribute(i).type() == attType) {
                this.deleteAttributeAt(i);
            }
            else {
                ++i;
            }
        }
    }
    
    public void deleteStringAttributes() {
        this.deleteAttributeType(2);
    }
    
    public void deleteWithMissing(final int attIndex) {
        final ArrayList<Instance> newInstances = new ArrayList<Instance>(this.numInstances());
        for (int i = 0; i < this.numInstances(); ++i) {
            if (!this.instance(i).isMissing(attIndex)) {
                newInstances.add(this.instance(i));
            }
        }
        this.m_Instances = newInstances;
    }
    
    public void deleteWithMissing(final Attribute att) {
        this.deleteWithMissing(att.index());
    }
    
    public void deleteWithMissingClass() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        this.deleteWithMissing(this.m_ClassIndex);
    }
    
    public Enumeration<Attribute> enumerateAttributes() {
        return new WekaEnumeration<Attribute>(this.m_Attributes, this.m_ClassIndex);
    }
    
    public Enumeration<Instance> enumerateInstances() {
        return new WekaEnumeration<Instance>(this.m_Instances);
    }
    
    public String equalHeadersMsg(final Instances dataset) {
        if (this.m_ClassIndex != dataset.m_ClassIndex) {
            return "Class index differ: " + (this.m_ClassIndex + 1) + " != " + (dataset.m_ClassIndex + 1);
        }
        if (this.m_Attributes.size() != dataset.m_Attributes.size()) {
            return "Different number of attributes: " + this.m_Attributes.size() + " != " + dataset.m_Attributes.size();
        }
        for (int i = 0; i < this.m_Attributes.size(); ++i) {
            final String msg = this.attribute(i).equalsMsg(dataset.attribute(i));
            if (msg != null) {
                return "Attributes differ at position " + (i + 1) + ":\n" + msg;
            }
        }
        return null;
    }
    
    public boolean equalHeaders(final Instances dataset) {
        return this.equalHeadersMsg(dataset) == null;
    }
    
    public Instance firstInstance() {
        return this.m_Instances.get(0);
    }
    
    public Random getRandomNumberGenerator(final long seed) {
        final Random r = new Random(seed);
        r.setSeed(this.instance(r.nextInt(this.numInstances())).toStringNoWeight().hashCode() + seed);
        return r;
    }
    
    public void insertAttributeAt(Attribute att, final int position) {
        if (position < 0 || position > this.m_Attributes.size()) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (this.attribute(att.name()) != null) {
            throw new IllegalArgumentException("Attribute name '" + att.name() + "' already in use at position #" + this.attribute(att.name()).index());
        }
        att = (Attribute)att.copy();
        att.setIndex(position);
        final ArrayList<Attribute> newList = new ArrayList<Attribute>(this.m_Attributes.size() + 1);
        final HashMap<String, Integer> newMap = new HashMap<String, Integer>((int)((this.m_Attributes.size() + 1) / 0.75));
        for (int i = 0; i < position; ++i) {
            final Attribute oldAtt = this.m_Attributes.get(i);
            newList.add(oldAtt);
            newMap.put(oldAtt.name(), i);
        }
        newList.add(att);
        newMap.put(att.name(), position);
        for (int i = position; i < this.m_Attributes.size(); ++i) {
            final Attribute newAtt = (Attribute)this.m_Attributes.get(i).copy();
            newAtt.setIndex(i + 1);
            newList.add(newAtt);
            newMap.put(newAtt.name(), i + 1);
        }
        this.m_Attributes = newList;
        this.m_NamesToAttributeIndices = newMap;
        for (int i = 0; i < this.numInstances(); ++i) {
            this.instance(i).setDataset(null);
            this.instance(i).insertAttributeAt(position);
            this.instance(i).setDataset(this);
        }
        if (this.m_ClassIndex >= position) {
            ++this.m_ClassIndex;
        }
    }
    
    public Instance instance(final int index) {
        return this.m_Instances.get(index);
    }
    
    @Override
    public Instance get(final int index) {
        return this.m_Instances.get(index);
    }
    
    public double kthSmallestValue(final Attribute att, final int k) {
        return this.kthSmallestValue(att.index(), k);
    }
    
    public double kthSmallestValue(final int attIndex, final int k) {
        if (!this.attribute(attIndex).isNumeric()) {
            throw new IllegalArgumentException("Instances: attribute must be numeric to compute kth-smallest value.");
        }
        if (k < 1 || k > this.numInstances()) {
            throw new IllegalArgumentException("Instances: value for k for computing kth-smallest value too large.");
        }
        final double[] vals = new double[this.numInstances()];
        for (int i = 0; i < vals.length; ++i) {
            final double val = this.instance(i).value(attIndex);
            if (Utils.isMissingValue(val)) {
                vals[i] = Double.MAX_VALUE;
            }
            else {
                vals[i] = val;
            }
        }
        return Utils.kthSmallestValue(vals, k);
    }
    
    public Instance lastInstance() {
        return this.m_Instances.get(this.m_Instances.size() - 1);
    }
    
    public double meanOrMode(final int attIndex) {
        if (this.attribute(attIndex).isNumeric()) {
            double result;
            double found = result = 0.0;
            for (int j = 0; j < this.numInstances(); ++j) {
                if (!this.instance(j).isMissing(attIndex)) {
                    found += this.instance(j).weight();
                    result += this.instance(j).weight() * this.instance(j).value(attIndex);
                }
            }
            if (found <= 0.0) {
                return 0.0;
            }
            return result / found;
        }
        else {
            if (this.attribute(attIndex).isNominal()) {
                final int[] counts = new int[this.attribute(attIndex).numValues()];
                for (int j = 0; j < this.numInstances(); ++j) {
                    if (!this.instance(j).isMissing(attIndex)) {
                        final int[] array = counts;
                        final int n = (int)this.instance(j).value(attIndex);
                        array[n] += (int)this.instance(j).weight();
                    }
                }
                return Utils.maxIndex(counts);
            }
            return 0.0;
        }
    }
    
    public double meanOrMode(final Attribute att) {
        return this.meanOrMode(att.index());
    }
    
    public int numAttributes() {
        return this.m_Attributes.size();
    }
    
    public int numClasses() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        if (!this.classAttribute().isNominal()) {
            return 1;
        }
        return this.classAttribute().numValues();
    }
    
    public int numDistinctValues(final int attIndex) {
        final HashSet<Double> set = new HashSet<Double>(2 * this.numInstances());
        for (final Instance current : this) {
            final double key = current.value(attIndex);
            if (!Utils.isMissingValue(key)) {
                set.add(key);
            }
        }
        return set.size();
    }
    
    public int numDistinctValues(final Attribute att) {
        return this.numDistinctValues(att.index());
    }
    
    public int numInstances() {
        return this.m_Instances.size();
    }
    
    @Override
    public int size() {
        return this.m_Instances.size();
    }
    
    public void randomize(final Random random) {
        for (int j = this.numInstances() - 1; j > 0; --j) {
            this.swap(j, random.nextInt(j + 1));
        }
    }
    
    @Deprecated
    public boolean readInstance(final Reader reader) throws IOException {
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, this, this.m_Lines, 1, new String[0]);
        final Instance inst = arff.readInstance(arff.getData(), false);
        this.m_Lines = arff.getLineNo();
        if (inst != null) {
            this.add(inst);
            return true;
        }
        return false;
    }
    
    public void replaceAttributeAt(Attribute att, final int position) {
        if (position < 0 || position > this.m_Attributes.size()) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (!att.name().equals(this.m_Attributes.get(position).name())) {
            final Attribute candidate = this.attribute(att.name());
            if (candidate != null && position != candidate.index()) {
                throw new IllegalArgumentException("Attribute name '" + att.name() + "' already in use at position #" + this.attribute(att.name()).index());
            }
        }
        att = (Attribute)att.copy();
        att.setIndex(position);
        final ArrayList<Attribute> newList = new ArrayList<Attribute>(this.m_Attributes.size());
        final HashMap<String, Integer> newMap = new HashMap<String, Integer>((int)((this.m_Attributes.size() + 1) / 0.75));
        for (int i = 0; i < position; ++i) {
            final Attribute oldAtt = this.m_Attributes.get(i);
            newList.add(oldAtt);
            newMap.put(oldAtt.name(), i);
        }
        newList.add(att);
        newMap.put(att.name(), position);
        for (int i = position + 1; i < this.m_Attributes.size(); ++i) {
            final Attribute newAtt = this.m_Attributes.get(i);
            newList.add(newAtt);
            newMap.put(newAtt.name(), i);
        }
        this.m_Attributes = newList;
        this.m_NamesToAttributeIndices = newMap;
        for (int i = 0; i < this.numInstances(); ++i) {
            this.instance(i).setDataset(null);
            this.instance(i).setMissing(position);
            this.instance(i).setDataset(this);
        }
        if (this.m_ClassIndex >= position) {
            ++this.m_ClassIndex;
        }
    }
    
    public String relationName() {
        return this.m_RelationName;
    }
    
    @Override
    public Instance remove(final int index) {
        return this.m_Instances.remove(index);
    }
    
    public void renameAttribute(final int att, final String name) {
        final Attribute existingAtt = this.attribute(name);
        if (existingAtt == null) {
            final Attribute newAtt = this.attribute(att).copy(name);
            final ArrayList<Attribute> newVec = new ArrayList<Attribute>(this.numAttributes());
            final HashMap<String, Integer> newMap = new HashMap<String, Integer>((int)(this.numAttributes() / 0.75));
            for (final Attribute attr : this.m_Attributes) {
                if (attr.index() == att) {
                    newVec.add(newAtt);
                    newMap.put(name, att);
                }
                else {
                    newVec.add(attr);
                    newMap.put(attr.name(), attr.index());
                }
            }
            this.m_Attributes = newVec;
            this.m_NamesToAttributeIndices = newMap;
            return;
        }
        if (att == existingAtt.index()) {
            return;
        }
        throw new IllegalArgumentException("Attribute name '" + name + "' already present at position #" + existingAtt.index());
    }
    
    public void renameAttribute(final Attribute att, final String name) {
        this.renameAttribute(att.index(), name);
    }
    
    public void renameAttributeValue(final int att, final int val, final String name) {
        final Attribute newAtt = (Attribute)this.attribute(att).copy();
        final ArrayList<Attribute> newVec = new ArrayList<Attribute>(this.numAttributes());
        newAtt.setValue(val, name);
        for (final Attribute attr : this.m_Attributes) {
            if (attr.index() == att) {
                newVec.add(newAtt);
            }
            else {
                newVec.add(attr);
            }
        }
        this.m_Attributes = newVec;
    }
    
    public void renameAttributeValue(final Attribute att, final String val, final String name) {
        final int v = att.indexOfValue(val);
        if (v == -1) {
            throw new IllegalArgumentException(String.valueOf(val) + " not found");
        }
        this.renameAttributeValue(att.index(), v, name);
    }
    
    public Instances resample(final Random random) {
        final Instances newData = new Instances(this, this.numInstances());
        while (newData.numInstances() < this.numInstances()) {
            newData.add(this.instance(random.nextInt(this.numInstances())));
        }
        return newData;
    }
    
    public Instances resampleWithWeights(final Random random) {
        return this.resampleWithWeights(random, false);
    }
    
    public Instances resampleWithWeights(final Random random, final boolean[] sampled) {
        return this.resampleWithWeights(random, sampled, false);
    }
    
    public Instances resampleWithWeights(final Random random, final boolean representUsingWeights) {
        return this.resampleWithWeights(random, null, representUsingWeights);
    }
    
    public Instances resampleWithWeights(final Random random, final boolean[] sampled, final boolean representUsingWeights) {
        final double[] weights = new double[this.numInstances()];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = this.instance(i).weight();
        }
        return this.resampleWithWeights(random, weights, sampled, representUsingWeights);
    }
    
    public Instances resampleWithWeights(final Random random, final double[] weights) {
        return this.resampleWithWeights(random, weights, null);
    }
    
    public Instances resampleWithWeights(final Random random, final double[] weights, final boolean[] sampled) {
        return this.resampleWithWeights(random, weights, sampled, false);
    }
    
    public Instances resampleWithWeights(final Random random, final double[] weights, final boolean[] sampled, final boolean representUsingWeights) {
        if (weights.length != this.numInstances()) {
            throw new IllegalArgumentException("weights.length != numInstances.");
        }
        final Instances newData = new Instances(this, this.numInstances());
        if (this.numInstances() == 0) {
            return newData;
        }
        final double[] P = new double[weights.length];
        System.arraycopy(weights, 0, P, 0, weights.length);
        Utils.normalize(P);
        final double[] Q = new double[weights.length];
        final int[] A = new int[weights.length];
        final int[] W = new int[weights.length];
        final int M = weights.length;
        int NN = -1;
        int NP = M;
        for (int I = 0; I < M; ++I) {
            if (P[I] < 0.0) {
                throw new IllegalArgumentException("Weights have to be positive.");
            }
            Q[I] = M * P[I];
            if (Q[I] < 1.0) {
                W[++NN] = I;
            }
            else {
                W[--NP] = I;
            }
        }
        if (NN > -1 && NP < M) {
            for (int S = 0; S < M - 1; ++S) {
                final int I2 = W[S];
                final int J = W[NP];
                A[I2] = J;
                final double[] array = Q;
                final int n = J;
                array[n] += Q[I2] - 1.0;
                if (Q[J] < 1.0) {
                    ++NP;
                }
                if (NP >= M) {
                    break;
                }
            }
        }
        for (int I = 0; I < M; ++I) {
            final double[] array2 = Q;
            final int n2 = I;
            array2[n2] += I;
        }
        int[] counts = null;
        if (representUsingWeights) {
            counts = new int[M];
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            final double U = M * random.nextDouble();
            final int I3 = (int)U;
            int ALRV;
            if (U < Q[I3]) {
                ALRV = I3;
            }
            else {
                ALRV = A[I3];
            }
            if (representUsingWeights) {
                final int[] array3 = counts;
                final int n3 = ALRV;
                ++array3[n3];
            }
            else {
                newData.add(this.instance(ALRV));
            }
            if (sampled != null) {
                sampled[ALRV] = true;
            }
            if (!representUsingWeights) {
                newData.instance(newData.numInstances() - 1).setWeight(1.0);
            }
        }
        if (representUsingWeights) {
            for (int i = 0; i < counts.length; ++i) {
                if (counts[i] > 0) {
                    newData.add(this.instance(i));
                    newData.instance(newData.numInstances() - 1).setWeight(counts[i]);
                }
            }
        }
        return newData;
    }
    
    @Override
    public Instance set(final int index, final Instance instance) {
        final Instance newInstance = (Instance)instance.copy();
        final Instance oldInstance = this.m_Instances.get(index);
        newInstance.setDataset(this);
        this.m_Instances.set(index, newInstance);
        return oldInstance;
    }
    
    public void setClass(final Attribute att) {
        this.m_ClassIndex = att.index();
    }
    
    public void setClassIndex(final int classIndex) {
        if (classIndex >= this.numAttributes()) {
            throw new IllegalArgumentException("Invalid class index: " + classIndex);
        }
        this.m_ClassIndex = classIndex;
    }
    
    public void setRelationName(final String newName) {
        this.m_RelationName = newName;
    }
    
    protected void sortBasedOnNominalAttribute(final int attIndex) {
        final int[] counts = new int[this.attribute(attIndex).numValues()];
        final Instance[] backup = new Instance[this.numInstances()];
        int j = 0;
        for (final Instance inst : this) {
            backup[j++] = inst;
            if (!inst.isMissing(attIndex)) {
                final int[] array = counts;
                final int n = (int)inst.value(attIndex);
                ++array[n];
            }
        }
        final int[] indices = new int[counts.length];
        int start = 0;
        for (int i = 0; i < counts.length; ++i) {
            indices[i] = start;
            start += counts[i];
        }
        Instance[] array2;
        for (int length = (array2 = backup).length, k = 0; k < length; ++k) {
            final Instance inst2 = array2[k];
            if (!inst2.isMissing(attIndex)) {
                this.m_Instances.set(indices[(int)inst2.value(attIndex)]++, inst2);
            }
            else {
                this.m_Instances.set(start++, inst2);
            }
        }
    }
    
    public void sort(final int attIndex) {
        if (!this.attribute(attIndex).isNominal()) {
            final double[] vals = new double[this.numInstances()];
            final Instance[] backup = new Instance[vals.length];
            for (int i = 0; i < vals.length; ++i) {
                final Instance inst = this.instance(i);
                backup[i] = inst;
                final double val = inst.value(attIndex);
                if (Utils.isMissingValue(val)) {
                    vals[i] = Double.MAX_VALUE;
                }
                else {
                    vals[i] = val;
                }
            }
            final int[] sortOrder = Utils.sortWithNoMissingValues(vals);
            for (int j = 0; j < vals.length; ++j) {
                this.m_Instances.set(j, backup[sortOrder[j]]);
            }
        }
        else {
            this.sortBasedOnNominalAttribute(attIndex);
        }
    }
    
    public void sort(final Attribute att) {
        this.sort(att.index());
    }
    
    public void stableSort(final int attIndex) {
        if (!this.attribute(attIndex).isNominal()) {
            final double[] vals = new double[this.numInstances()];
            final Instance[] backup = new Instance[vals.length];
            for (int i = 0; i < vals.length; ++i) {
                final Instance inst = this.instance(i);
                backup[i] = inst;
                vals[i] = inst.value(attIndex);
            }
            final int[] sortOrder = Utils.stableSort(vals);
            for (int j = 0; j < vals.length; ++j) {
                this.m_Instances.set(j, backup[sortOrder[j]]);
            }
        }
        else {
            this.sortBasedOnNominalAttribute(attIndex);
        }
    }
    
    public void stableSort(final Attribute att) {
        this.stableSort(att.index());
    }
    
    public void stratify(final int numFolds) {
        if (numFolds <= 1) {
            throw new IllegalArgumentException("Number of folds must be greater than 1");
        }
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        if (this.classAttribute().isNominal()) {
            for (int index = 1; index < this.numInstances(); ++index) {
                final Instance instance1 = this.instance(index - 1);
                for (int j = index; j < this.numInstances(); ++j) {
                    final Instance instance2 = this.instance(j);
                    if (instance1.classValue() == instance2.classValue() || (instance1.classIsMissing() && instance2.classIsMissing())) {
                        this.swap(index, j);
                        ++index;
                    }
                }
            }
            this.stratStep(numFolds);
        }
    }
    
    public double sumOfWeights() {
        double sum = 0.0;
        for (int i = 0; i < this.numInstances(); ++i) {
            sum += this.instance(i).weight();
        }
        return sum;
    }
    
    public Instances testCV(final int numFolds, final int numFold) {
        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > this.numInstances()) {
            throw new IllegalArgumentException("Can't have more folds than instances!");
        }
        int numInstForFold = this.numInstances() / numFolds;
        int offset;
        if (numFold < this.numInstances() % numFolds) {
            ++numInstForFold;
            offset = numFold;
        }
        else {
            offset = this.numInstances() % numFolds;
        }
        final Instances test = new Instances(this, numInstForFold);
        final int first = numFold * (this.numInstances() / numFolds) + offset;
        this.copyInstances(first, test, numInstForFold);
        return test;
    }
    
    @Override
    public String toString() {
        final StringBuffer text = new StringBuffer();
        text.append("@relation").append(" ").append(Utils.quote(this.m_RelationName)).append("\n\n");
        for (int i = 0; i < this.numAttributes(); ++i) {
            text.append(this.attribute(i)).append("\n");
        }
        text.append("\n").append("@data").append("\n");
        text.append(this.stringWithoutHeader());
        return text.toString();
    }
    
    protected String stringWithoutHeader() {
        final StringBuffer text = new StringBuffer();
        for (int i = 0; i < this.numInstances(); ++i) {
            text.append(this.instance(i));
            if (i < this.numInstances() - 1) {
                text.append('\n');
            }
        }
        return text.toString();
    }
    
    public Instances trainCV(final int numFolds, final int numFold) {
        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > this.numInstances()) {
            throw new IllegalArgumentException("Can't have more folds than instances!");
        }
        int numInstForFold = this.numInstances() / numFolds;
        int offset;
        if (numFold < this.numInstances() % numFolds) {
            ++numInstForFold;
            offset = numFold;
        }
        else {
            offset = this.numInstances() % numFolds;
        }
        final Instances train = new Instances(this, this.numInstances() - numInstForFold);
        final int first = numFold * (this.numInstances() / numFolds) + offset;
        this.copyInstances(0, train, first);
        this.copyInstances(first + numInstForFold, train, this.numInstances() - first - numInstForFold);
        return train;
    }
    
    public Instances trainCV(final int numFolds, final int numFold, final Random random) {
        final Instances train = this.trainCV(numFolds, numFold);
        train.randomize(random);
        return train;
    }
    
    public double[] variances() {
        final double[] vars = new double[this.numAttributes()];
        for (int i = 0; i < this.numAttributes(); ++i) {
            vars[i] = Double.NaN;
        }
        final double[] means = new double[this.numAttributes()];
        final double[] sumWeights = new double[this.numAttributes()];
        for (int j = 0; j < this.numInstances(); ++j) {
            final double weight = this.instance(j).weight();
            for (int attIndex = 0; attIndex < this.numAttributes(); ++attIndex) {
                if (this.attribute(attIndex).isNumeric() && !this.instance(j).isMissing(attIndex)) {
                    final double value = this.instance(j).value(attIndex);
                    if (Double.isNaN(vars[attIndex])) {
                        means[attIndex] = value;
                        sumWeights[attIndex] = weight;
                        vars[attIndex] = 0.0;
                    }
                    else {
                        final double delta = weight * (value - means[attIndex]);
                        final double[] array = sumWeights;
                        final int n = attIndex;
                        array[n] += weight;
                        final double[] array2 = means;
                        final int n2 = attIndex;
                        array2[n2] += delta / sumWeights[attIndex];
                        final double[] array3 = vars;
                        final int n3 = attIndex;
                        array3[n3] += delta * (value - means[attIndex]);
                    }
                }
            }
        }
        for (int attIndex2 = 0; attIndex2 < this.numAttributes(); ++attIndex2) {
            if (this.attribute(attIndex2).isNumeric()) {
                if (sumWeights[attIndex2] <= 1.0) {
                    vars[attIndex2] = Double.NaN;
                }
                else {
                    final double[] array4 = vars;
                    final int n4 = attIndex2;
                    array4[n4] /= sumWeights[attIndex2] - 1.0;
                    if (vars[attIndex2] < 0.0) {
                        vars[attIndex2] = 0.0;
                    }
                }
            }
        }
        return vars;
    }
    
    public double variance(final int attIndex) {
        if (!this.attribute(attIndex).isNumeric()) {
            throw new IllegalArgumentException("Can't compute variance because attribute is not numeric!");
        }
        double mean = 0.0;
        double var = Double.NaN;
        double sumWeights = 0.0;
        for (int i = 0; i < this.numInstances(); ++i) {
            if (!this.instance(i).isMissing(attIndex)) {
                final double weight = this.instance(i).weight();
                final double value = this.instance(i).value(attIndex);
                if (Double.isNaN(var)) {
                    mean = value;
                    sumWeights = weight;
                    var = 0.0;
                }
                else {
                    final double delta = weight * (value - mean);
                    sumWeights += weight;
                    mean += delta / sumWeights;
                    var += delta * (value - mean);
                }
            }
        }
        if (sumWeights <= 1.0) {
            return Double.NaN;
        }
        var /= sumWeights - 1.0;
        if (var < 0.0) {
            return 0.0;
        }
        return var;
    }
    
    public double variance(final Attribute att) {
        return this.variance(att.index());
    }
    
    public AttributeStats attributeStats(final int index) {
        final AttributeStats result = new AttributeStats();
        if (this.attribute(index).isNominal()) {
            result.nominalCounts = new int[this.attribute(index).numValues()];
            result.nominalWeights = new double[this.attribute(index).numValues()];
        }
        if (this.attribute(index).isNumeric()) {
            result.numericStats = new Stats();
        }
        result.totalCount = this.numInstances();
        final HashMap<Double, double[]> map = new HashMap<Double, double[]>(2 * result.totalCount);
        for (final Instance current : this) {
            final double key = current.value(index);
            if (Utils.isMissingValue(key)) {
                final AttributeStats attributeStats = result;
                ++attributeStats.missingCount;
            }
            else {
                double[] values = map.get(key);
                if (values == null) {
                    values = new double[] { 1.0, current.weight() };
                    map.put(key, values);
                }
                else {
                    final double[] array = values;
                    final int n = 0;
                    ++array[n];
                    final double[] array2 = values;
                    final int n2 = 1;
                    array2[n2] += current.weight();
                }
            }
        }
        for (final Map.Entry<Double, double[]> entry : map.entrySet()) {
            result.addDistinct(entry.getKey(), (int)entry.getValue()[0], entry.getValue()[1]);
        }
        return result;
    }
    
    public double[] attributeToDoubleArray(final int index) {
        final double[] result = new double[this.numInstances()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.instance(i).value(index);
        }
        return result;
    }
    
    public String toSummaryString() {
        final StringBuffer result = new StringBuffer();
        result.append("Relation Name:  ").append(this.relationName()).append('\n');
        result.append("Num Instances:  ").append(this.numInstances()).append('\n');
        result.append("Num Attributes: ").append(this.numAttributes()).append('\n');
        result.append('\n');
        result.append(Utils.padLeft("", 5)).append(Utils.padRight("Name", 25));
        result.append(Utils.padLeft("Type", 5)).append(Utils.padLeft("Nom", 5));
        result.append(Utils.padLeft("Int", 5)).append(Utils.padLeft("Real", 5));
        result.append(Utils.padLeft("Missing", 12));
        result.append(Utils.padLeft("Unique", 12));
        result.append(Utils.padLeft("Dist", 6)).append('\n');
        final int numDigits = (int)Math.log10(this.numAttributes()) + 1;
        for (int i = 0; i < this.numAttributes(); ++i) {
            final Attribute a = this.attribute(i);
            final AttributeStats as = this.attributeStats(i);
            result.append(Utils.padLeft(new StringBuilder().append(i + 1).toString(), numDigits)).append(' ');
            result.append(Utils.padRight(a.name(), 25)).append(' ');
            switch (a.type()) {
                case 1: {
                    result.append(Utils.padLeft("Nom", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
                case 0: {
                    result.append(Utils.padLeft("Num", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
                case 3: {
                    result.append(Utils.padLeft("Dat", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
                case 2: {
                    result.append(Utils.padLeft("Str", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
                case 4: {
                    result.append(Utils.padLeft("Rel", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
                default: {
                    result.append(Utils.padLeft("???", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
                    break;
                }
            }
            result.append(Utils.padLeft(new StringBuilder().append(as.missingCount).toString(), 5)).append(" /");
            long percent = Math.round(100.0 * as.missingCount / as.totalCount);
            result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
            result.append(Utils.padLeft(new StringBuilder().append(as.uniqueCount).toString(), 5)).append(" /");
            percent = Math.round(100.0 * as.uniqueCount / as.totalCount);
            result.append(Utils.padLeft(new StringBuilder().append(percent).toString(), 3)).append("% ");
            result.append(Utils.padLeft(new StringBuilder().append(as.distinctCount).toString(), 5)).append(' ');
            result.append('\n');
        }
        return result.toString();
    }
    
    protected void copyInstances(final int from, final Instances dest, final int num) {
        for (int i = 0; i < num; ++i) {
            dest.add(this.instance(from + i));
        }
    }
    
    protected String instancesAndWeights() {
        final StringBuffer text = new StringBuffer();
        for (int i = 0; i < this.numInstances(); ++i) {
            text.append(this.instance(i) + " " + this.instance(i).weight());
            if (i < this.numInstances() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }
    
    protected void stratStep(final int numFolds) {
        final ArrayList<Instance> newVec = new ArrayList<Instance>(this.m_Instances.size());
        int start = 0;
        while (newVec.size() < this.numInstances()) {
            for (int j = start; j < this.numInstances(); j += numFolds) {
                newVec.add(this.instance(j));
            }
            ++start;
        }
        this.m_Instances = newVec;
    }
    
    public void swap(final int i, final int j) {
        final Instance in = this.m_Instances.get(i);
        this.m_Instances.set(i, this.m_Instances.get(j));
        this.m_Instances.set(j, in);
    }
    
    public static Instances mergeInstances(final Instances first, final Instances second) {
        if (first.numInstances() != second.numInstances()) {
            throw new IllegalArgumentException("Instance sets must be of the same size");
        }
        final ArrayList<Attribute> newAttributes = new ArrayList<Attribute>(first.numAttributes() + second.numAttributes());
        for (final Attribute att : first.m_Attributes) {
            newAttributes.add(att);
        }
        for (final Attribute att : second.m_Attributes) {
            newAttributes.add((Attribute)att.copy());
        }
        final Instances merged = new Instances(String.valueOf(first.relationName()) + '_' + second.relationName(), newAttributes, first.numInstances());
        for (int i = 0; i < first.numInstances(); ++i) {
            merged.add(first.instance(i).mergeInstance(second.instance(i)));
        }
        return merged;
    }
    
    public static void test(final String[] argv) {
        final Random random = new Random(2L);
        try {
            if (argv.length > 1) {
                throw new Exception("Usage: Instances [<filename>]");
            }
            final ArrayList<String> testVals = new ArrayList<String>(2);
            testVals.add("first_value");
            testVals.add("second_value");
            final ArrayList<Attribute> testAtts = new ArrayList<Attribute>(2);
            testAtts.add(new Attribute("nominal_attribute", testVals));
            testAtts.add(new Attribute("numeric_attribute"));
            Instances instances = new Instances("test_set", testAtts, 10);
            instances.add(new DenseInstance(instances.numAttributes()));
            instances.add(new DenseInstance(instances.numAttributes()));
            instances.add(new DenseInstance(instances.numAttributes()));
            instances.setClassIndex(0);
            System.out.println("\nSet of instances created from scratch:\n");
            System.out.println(instances);
            if (argv.length == 1) {
                final String filename = argv[0];
                Reader reader = new FileReader(filename);
                System.out.println("\nFirst five instances from file:\n");
                instances = new Instances(reader, 1);
                instances.setClassIndex(instances.numAttributes() - 1);
                for (int i = 0; i < 5 && instances.readInstance(reader); ++i) {}
                System.out.println(instances);
                reader = new FileReader(filename);
                instances = new Instances(reader);
                instances.setClassIndex(instances.numAttributes() - 1);
                System.out.println("\nDataset:\n");
                System.out.println(instances);
                System.out.println("\nClass index: " + instances.classIndex());
            }
            System.out.println("\nClass name: " + instances.classAttribute().name());
            System.out.println("\nClass index: " + instances.classIndex());
            System.out.println("\nClass is nominal: " + instances.classAttribute().isNominal());
            System.out.println("\nClass is numeric: " + instances.classAttribute().isNumeric());
            System.out.println("\nClasses:\n");
            for (int i = 0; i < instances.numClasses(); ++i) {
                System.out.println(instances.classAttribute().value(i));
            }
            System.out.println("\nClass values and labels of instances:\n");
            for (int i = 0; i < instances.numInstances(); ++i) {
                final Instance inst = instances.instance(i);
                System.out.print(String.valueOf(inst.classValue()) + "\t");
                System.out.print(inst.toString(inst.classIndex()));
                if (instances.instance(i).classIsMissing()) {
                    System.out.println("\tis missing");
                }
                else {
                    System.out.println();
                }
            }
            System.out.println("\nCreating random weights for instances.");
            for (int i = 0; i < instances.numInstances(); ++i) {
                instances.instance(i).setWeight(random.nextDouble());
            }
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
            Instances secondInstances = new Instances(instances);
            final Attribute testAtt = new Attribute("Inserted");
            secondInstances.insertAttributeAt(testAtt, 0);
            System.out.println("\nSet with inserted attribute:\n");
            System.out.println(secondInstances);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            secondInstances.deleteAttributeAt(0);
            System.out.println("\nSet with attribute deleted:\n");
            System.out.println(secondInstances);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            System.out.println("\nHeaders equal: " + instances.equalHeaders(secondInstances) + "\n");
            System.out.println("\nData (internal values):\n");
            for (int i = 0; i < instances.numInstances(); ++i) {
                for (int j = 0; j < instances.numAttributes(); ++j) {
                    if (instances.instance(i).isMissing(j)) {
                        System.out.print("? ");
                    }
                    else {
                        System.out.print(String.valueOf(instances.instance(i).value(j)) + " ");
                    }
                }
                System.out.println();
            }
            System.out.println("\nEmpty dataset:\n");
            final Instances empty = new Instances(instances, 0);
            System.out.println(empty);
            System.out.println("\nClass name: " + empty.classAttribute().name());
            if (empty.classAttribute().isNominal()) {
                final Instances copy = new Instances(empty, 0);
                copy.renameAttribute(copy.classAttribute(), "new_name");
                copy.renameAttributeValue(copy.classAttribute(), copy.classAttribute().value(0), "new_val_name");
                System.out.println("\nDataset with names changed:\n" + copy);
                System.out.println("\nOriginal dataset:\n" + empty);
            }
            final int start = instances.numInstances() / 4;
            final int num = instances.numInstances() / 2;
            System.out.print("\nSubset of dataset: ");
            System.out.println(String.valueOf(num) + " instances from " + (start + 1) + ". instance");
            secondInstances = new Instances(instances, start, num);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            System.out.println("\nInstances and their weights:\n");
            System.out.println(secondInstances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(secondInstances.sumOfWeights());
            System.out.println("\nTrain and test folds for 3-fold CV:");
            if (instances.classAttribute().isNominal()) {
                instances.stratify(3);
            }
            for (int j = 0; j < 3; ++j) {
                final Instances train = instances.trainCV(3, j, new Random(1L));
                final Instances test = instances.testCV(3, j);
                System.out.println("\nTrain: ");
                System.out.println("\nInstances and their weights:\n");
                System.out.println(train.instancesAndWeights());
                System.out.print("\nSum of weights: ");
                System.out.println(train.sumOfWeights());
                System.out.println("\nClass name: " + train.classAttribute().name());
                System.out.println("\nTest: ");
                System.out.println("\nInstances and their weights:\n");
                System.out.println(test.instancesAndWeights());
                System.out.print("\nSum of weights: ");
                System.out.println(test.sumOfWeights());
                System.out.println("\nClass name: " + test.classAttribute().name());
            }
            System.out.println("\nRandomized dataset:");
            instances.randomize(random);
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
            System.out.print("\nInstances sorted according to first attribute:\n ");
            instances.sort(0);
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String[] args) {
        try {
            if (args.length == 0) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.in);
                final Instances i = source.getDataSet();
                System.out.println(i.toSummaryString());
            }
            else if (args.length == 1 && !args[0].equals("-h") && !args[0].equals("help")) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
                final Instances i = source.getDataSet();
                System.out.println(i.toSummaryString());
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("merge")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                final Instances i = mergeInstances(source2.getDataSet(), source3.getDataSet());
                System.out.println(i);
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("append")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                final String msg = source2.getStructure().equalHeadersMsg(source3.getStructure());
                if (msg != null) {
                    throw new Exception("The two datasets have different headers:\n" + msg);
                }
                Instances structure = source2.getStructure();
                System.out.println(source2.getStructure());
                while (source2.hasMoreElements(structure)) {
                    System.out.println(source2.nextElement(structure));
                }
                structure = source3.getStructure();
                while (source3.hasMoreElements(structure)) {
                    System.out.println(source3.nextElement(structure));
                }
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("headers")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                final String msg = source2.getStructure().equalHeadersMsg(source3.getStructure());
                if (msg == null) {
                    System.out.println("Headers match");
                }
                else {
                    System.out.println("Headers don't match:\n" + msg);
                }
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("randomize")) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[2]);
                final Instances i = source.getDataSet();
                i.randomize(new Random(Integer.parseInt(args[1])));
                System.out.println(i);
            }
            else {
                System.err.println("\nUsage:\n\tweka.core.Instances help\n\t\tPrints this help\n\tweka.core.Instances <filename>\n\t\tOutputs dataset statistics\n\tweka.core.Instances merge <filename1> <filename2>\n\t\tMerges the datasets (must have same number of rows).\n\t\tGenerated dataset gets output on stdout.\n\tweka.core.Instances append <filename1> <filename2>\n\t\tAppends the second dataset to the first (must have same number of attributes).\n\t\tGenerated dataset gets output on stdout.\n\tweka.core.Instances headers <filename1> <filename2>\n\t\tCompares the structure of the two datasets and outputs whether they\n\t\tdiffer or not.\n\tweka.core.Instances randomize <seed> <filename>\n\t\tRandomizes the dataset and outputs it on stdout.\n");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
    
    @Override
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 12038 $");
    }
}
