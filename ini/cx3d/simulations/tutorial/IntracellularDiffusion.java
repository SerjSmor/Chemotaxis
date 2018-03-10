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

import static ini.cx3d.utilities.Matrix.randomNoise;

import com.sun.glass.events.KeyEvent;

import static ini.cx3d.utilities.Matrix.add;
import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.AbstractLocalBiologyModule;
import ini.cx3d.localBiology.CellElement;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.localBiology.SomaElement;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.utilities.Matrix;


public class IntracellularDiffusion extends AbstractLocalBiologyModule{
	
	double direction[] = randomNoise(1.0, 3); // initial direction
	
	
	
	public static void main(String[] args) {

		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(1L);
		for (int i = 0; i < 200; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(500,3));
		}
		
		// defining the templates for the intracellular substance
		double D = 1000; // diffusion cst
		double d = 0.01;	// degradation cst
		IntracellularSubstance tubulin = new IntracellularSubstance("tubulin",D,d);
		tubulin.setVolumeDependant(false);
		tubulin.setVisibleFromOutside(true);
		tubulin.setDiffusionConstant(D);
		tubulin.setDegradationConstant(d);
		ecm.addNewIntracellularSubstanceTemplate(tubulin);
		// getting a cell
		Cell c = CellFactory.getCellInstance(new double[] {0,0,50});
		c.setColorForAllPhysicalObjects(Param.BLUE);
		// insert production module
		SomaElement soma = c.getSomaElement();
		soma.addLocalBiologyModule(new InternalSecretor());
		//insert growth cone module
		NeuriteElement ne = c.getSomaElement().extendNewNeurite(new double[] {0,1,1});
		ne.getPhysical().setDiameter(2.0);
		ne.addLocalBiologyModule(new GrowthCone());
		
		Cell d1 = CellFactory.getCellInstance(new double[] {50,30,0});
		c.setColorForAllPhysicalObjects(Param.BLUE);
		// insert production module
		SomaElement soma1 = d1.getSomaElement();
		soma1.addLocalBiologyModule(new InternalSecretor());
		//insert growth cone module
		NeuriteElement ne1 = d1.getSomaElement().extendNewNeurite(new double[] {1,0,1});
		ne1.getPhysical().setDiameter(2.0);
		ne1.addLocalBiologyModule(new GrowthCone());
		// add movement
//		
//		
//		Cell d11 = CellFactory.getCellInstance(new double[] {-50,30,0});
//		c.setColorForAllPhysicalObjects(Param.BLUE);
//		// insert production module
//		SomaElement soma11 = d11.getSomaElement();
//		soma11.addLocalBiologyModule(new InternalSecretor());
//		//insert growth cone module
//		NeuriteElement ne11 = d11.getSomaElement().extendNewNeurite(new double[] {0,1,0});
//		ne11.getPhysical().setDiameter(2.0);
//		ne11.addLocalBiologyModule(new GrowthCone());
//		
//		Cell d111 = CellFactory.getCellInstance(new double[] {-75,70,75});
//		c.setColorForAllPhysicalObjects(Param.BLUE);
//		// insert production module
//		SomaElement soma111 = d111.getSomaElement();
//		soma111.addLocalBiologyModule(new InternalSecretor());
//		//insert growth cone module
//		NeuriteElement ne111 = d111.getSomaElement().extendNewNeurite(new double[] {0,0,1});
//		ne111.getPhysical().setDiameter(2.0);
//		ne111.addLocalBiologyModule(new GrowthCone());
//		
//		
//		
		
		
		// run, Forrest, run..
		ini.cx3d.utilities.SystemUtilities.tic();
		for (int i = 0; i < 10000; i++) {
			Scheduler.simulateOneStep();
		}
		ini.cx3d.utilities.SystemUtilities.tac();
		
		
	}

	public static class InternalSecretor extends AbstractLocalBiologyModule {

		// secretion rate (quantity/time)
		private double secretionRate = 4000;  
		
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
	
	public static class GrowthCone extends AbstractLocalBiologyModule{
		
		// some parameters 
		private static double speedFactor = 200;	
		private static double consumptionFactor = 100;
		private static double bifurcationProba = 0.003;
		// direction at previous time step:
		private double[] previousDir;
		// initial direction is parallel to the cylinder axis
		// therefore we overwrite this method from the superclass:
		public void setCellElement(CellElement cellElement){
			super.cellElement = cellElement;
			this.previousDir = cellElement.getPhysical().getAxis();
		}
		// to ensure distribution in all terminal segments:
		public AbstractLocalBiologyModule getCopy() {return new GrowthCone();}

		public boolean isCopiedWhenNeuriteBranches() {return true;}
		
		public boolean isDeletedAfterNeuriteHasBifurcated() {return true;}
		
		// growth cone model
		public void run() {
			// getting the concentration and defining the speed
			PhysicalObject cyl = super.cellElement.getPhysical();
			double concentration = cyl.getIntracellularConcentration("tubulin");
			
			double speed = concentration*speedFactor;
			if(speed>100)  // can't be faster than 100
				speed = 100;
			// movement and consumption
			double[] direction = Matrix.add(previousDir, randomNoise(0.1,3));
			previousDir = Matrix.normalize(direction);
			cyl.movePointMass(speed, direction);
			cyl.modifyIntracellularQuantity("tubulin", -concentration*consumptionFactor);
			// test for bifurcation
//			if(ECM.getRandomDouble()<bifurcationProba)
//				((NeuriteElement)(super.cellElement)).bifurcate();
		}
	}

	@Override
	public AbstractLocalBiologyModule getCopy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	
}
