/*
Copyright (C) 2009 Frédéric Zubler, Rodney J. Douglas,
Dennis Göhlsdorf, Toby Weston, Andreas Hauri, Roman Bauer,
Sabina Pfister & Adrian M. Whatley.

This file is part of CX3D.

CX3D is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

CX3D is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CX3D.  If not, see <http://www.gnu.org/licenses/>.
*/

package ini.cx3d.simulations.tutorial;

import static ini.cx3d.utilities.Matrix.add;
import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.randomNoise;

import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.AbstractLocalBiologyModule;
import ini.cx3d.localBiology.CellElement;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.localBiology.SomaElement;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.simulations.tutorial.IntracellularDiffusion.GrowthCone;
import ini.cx3d.simulations.tutorial.IntracellularDiffusion.InternalSecretor;
import ini.cx3d.utilities.Matrix;

public class SomaRandomWalkModuleJona extends AbstractLocalBiologyModule {
	
	double direction[] = randomNoise(1,3); // initial direction
	double constDirection[] = {1, 2, 3};
	static double firstCellInitialPlace[] = {-300, 30, 0};
	static double secondCellInitialPlace[] = {300, 30, 0};
	
	
	public AbstractLocalBiologyModule getCopy() {
		return new SomaRandomWalkModuleJona();
	}

	public void run() {
		double speed = 1;
//		double[] deltaDirection = randomNoise(1, 3);
//		direction = add(direction, deltaDirection);
//		direction = normalize(direction);

		//		depending on some state - we either move or stop 
//		super.cellElement.move(speed, constDirection);
		
	
	}
	
	public boolean isCopiedWhenSomaDivides() {
		return true;
	}

	public static void main(String[] args) {
		
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(1L);
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(10,3));
		}
		
		
		//for(int i = 0; i<1; i++){
			Cell c = CellFactory.getCellInstance(firstCellInitialPlace);
			c.setColorForAllPhysicalObjects(Param.BLUE);
			c.getSomaElement().addLocalBiologyModule(new InternalSecretor());
//			for linear walk
			c.getSomaElement().addLocalBiologyModule(new SomaRandomWalkModuleJona());
			//SomaElement soma1 = c.getSomaElement();
			//soma1.addLocalBiologyModule(new InternalSecretor());
			NeuriteElement ne = c.getSomaElement().extendNewNeurite(new double[] {0,1,1});
			ne.getPhysical().setDiameter(2.0);
			ne.addLocalBiologyModule(new GrowthCone(secondCellInitialPlace));
			
			
			Cell c1 = CellFactory.getCellInstance(secondCellInitialPlace);
//			c1.getSomaElement().addLocalBiologyModule(new InternalSecretor());
//			awesome for internal walkt
			c1.getSomaElement().addLocalBiologyModule(new SomaRandomWalkModuleJona());
			NeuriteElement ne1 = c1.getSomaElement().extendNewNeurite(new double[] {0,1,1});
			ne1.getPhysical().setDiameter(2.0);
			ne1.addLocalBiologyModule(new GrowthCone(firstCellInitialPlace));
			
			
			/* defining the templates for the intracellular substance
			double D = 2; // diffusion cst
			double d = 1.9;	// degradation cst
			IntracellularSubstance tubulin = new IntracellularSubstance("tubulin",D,d);
			tubulin.setVolumeDependant(true);
			tubulin.setVisibleFromOutside(false);
			tubulin.setDiffusionConstant(D);
			tubulin.setDegradationConstant(d);
			ecm.addNewIntracellularSubstanceTemplate(tubulin);*/
			
			//c.addCellModule(new DividingModule());  // un-comment to have the cells divide 
		//}
		Scheduler.simulate();
	}


	private static class InternalSecretor extends AbstractLocalBiologyModule {

		// secretion rate (quantity/time)
		private double secretionRate = 0;  
		
		// needed for copy in the cell in case of division
		public AbstractLocalBiologyModule getCopy() {
			return new InternalSecretor();
		}
		
		// method called at each time step: secretes tubulin in the extracellular space 
		public void run() {
			super.cellElement.getPhysical().modifyIntracellularQuantity(
					"tubulin", secretionRate);
		}
	}
	
	
	// this is the growth model of the axon
	// it should grow either towards the diffusian waterfall or just linear check
	
	public static class GrowthCone extends AbstractLocalBiologyModule{
		
		// some parameters 
		private static double speedFactor = 1;	
		private static double consumptionFactor = 10;
		private static double bifurcationProba = 0.003;
		// direction at previous time step:
		private double[] previousDir;
		private double[] goal;
		public GrowthCone(double[] goal) {
			this.goal = goal;
		}
		// initial direction is parallel to the cylinder axis
		// therefore we overwrite this method from the superclass:
		public void setCellElement(CellElement cellElement){
			super.cellElement = cellElement;
			this.previousDir = cellElement.getPhysical().getAxis();
		}
		// to ensure distribution in all terminal segments:
		public AbstractLocalBiologyModule getCopy() {return new GrowthCone(goal);}

		public boolean isCopiedWhenNeuriteBranches() {return true;}
		
		public boolean isDeletedAfterNeuriteHasBifurcated() {return true;}
		
		// growth cone model
		
		public void run() {
			// getting the concentration and defining the speed
			PhysicalObject cyl = super.cellElement.getPhysical();
			// SERJ - just move it
			// go towards the goal
//			double[] myDirection = {1, 2, 3};
			double[] currentLocation = cyl.getMassLocation();
			double currentLocationX = cyl.getMassLocation()[0];
			double speed = 4;
			double nextLocationX = 0;
			double goalX = goal[0];
			if (goalX < 0) {
				nextLocationX = currentLocationX - speed;
			} else {
				nextLocationX = currentLocationX + speed;
			}
			// not sure why it has to be negative, but it works
			double[] nextLocation = {-nextLocationX, currentLocation[1], currentLocation[2]};
			super.cellElement.move(speed, nextLocation);
			
			// this is the 
			// depending on some state we either move or stop
			
			
			//			double concentration = cyl.getIntracellularConcentration("tubulin");
//			double speed = concentration*speedFactor;
//			if(speed>100)  // can't be faster than 100
//				speed = 100;
//			// movement and consumption
//			double[] direction = Matrix.add(previousDir, randomNoise(0.1,3));
//			previousDir = Matrix.normalize(direction);
//			cyl.movePointMass(speed, direction);
//			cyl.modifyIntracellularQuantity("tubulin", -concentration*consumptionFactor);
//			// test for bifurcation
//			if(ECM.getRandomDouble()<bifurcationProba)
//				((NeuriteElement)(super.cellElement)).bifurcate();
		}
	}








}
