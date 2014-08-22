package org.probe.algo.rule;

import java.util.List;
import java.util.Stack;

import org.probe.algo.rule.id3.TreeNode;
import org.probe.data.DataAttribute;
import org.probe.data.DataModel;
import org.probe.data.DataSubModel;
import org.probe.data.util.DataSplitter;
import org.probe.rule.RuleModel;
import org.probe.stats.InformationGainCalculator;


public class ID3RuleLearner implements RuleLearner {

	@Override
	public void setDataModel(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public void runAlgo() throws Exception {
		TreeNode rootNode = createTreeFromDataModel();
		rootNode.printInOrder();
	}
	
	private TreeNode createTreeFromDataModel() throws Exception{
		TreeNode rootNode = new TreeNode(dataModel);

		Stack<TreeNode> treeNodeStack = new Stack<TreeNode>();
		createNode(dataModel,treeNodeStack,rootNode);
		
		while(treeNodeStack.size() > 0){
			TreeNode currentNode =  treeNodeStack.pop();
			DataSubModel dataSubModel = (DataSubModel)currentNode.getPayload();
			
			createNode(dataSubModel, treeNodeStack,currentNode);
		}
		
		return rootNode;
	}

	private void createNode(DataModel dataSubModel, Stack<TreeNode> treeNodeStack, TreeNode treeNode) throws Exception {
		double maxInformationGain = -1*Double.MAX_VALUE;
		DataSubModel[] currentSubModels = null;
		
		for(DataAttribute attribute : dataSubModel.getAttributes()){
			if(attribute.isClass() || attribute.isInstance())
				continue;
			
			DataSubModel[] subModels = DataSplitter.splitOnAttribute(dataSubModel, attribute);

			double informationGain = InformationGainCalculator
					.calculateInformationGainOnSubModels(dataSubModel, subModels);
			if(informationGain == 0){
				//nothing more to be done in this tree
				continue;
			}

			if(informationGain > maxInformationGain){
				maxInformationGain = informationGain;
				currentSubModels = subModels;
			}
		}
		
		if(currentSubModels != null){
			for(DataSubModel model : currentSubModels){				
				createTreeNodeChildFor(model, treeNode, treeNodeStack);
			}	
		}
	}
	
	private void createTreeNodeChildFor(DataSubModel model, TreeNode treeNode, Stack<TreeNode> treeNodeStack){
		TreeNode childNode = new TreeNode(model);
		treeNode.addChild(childNode);
		
		List<String> items = model.getClassLabels();
		if(items.size() == 1){
			String label = items.get(0);
			childNode.setLeaf(model.getClassAttribute().getAttributeName(),label);
		}else {
			treeNodeStack.push(childNode);	
		}
	}

	@Override
	public boolean hasLearntRules() {
		return ruleModel != null;
	}

	@Override
	public RuleModel getRuleModel() {
		return ruleModel;
	}	

	private DataModel dataModel = null;
	private RuleModel ruleModel = null;
}
