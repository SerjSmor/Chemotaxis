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

import java.awt.Rectangle;

import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.AbstractLocalBiologyModule;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.localBiology.NeuriteElement;

public class SomaClusteringJona extends AbstractLocalBiologyModule {
	
	private String substanceID;
	
	
	double direction[] = randomNoise(1,3); // initial direction
	static ECM ecm = ECM.getInstance();
	private double branchingFactor = 0.010;
	
	public SomaClusteringJona(String substanceID) {
		this.substanceID = substanceID;
		this.branchingFactor = branchingFactor;
	}
	
	public AbstractLocalBiologyModule getCopy() {
		return new SomaClusteringJona(substanceID);
	}

	@Override
	public boolean isCopiedWhenNeuriteBranches() {
		return true;
	}
	
	@Override
	public boolean isDeletedAfterNeuriteHasBifurcated() {
		return true;
	}
	
	
	
	public void run() {		
		PhysicalObject physical = super.cellElement.getPhysical();
		// move
		double speed = 10;
		double[] grad = physical .getExtracellularGradient(substanceID);
		physical.movePointMass(speed, normalize(grad));
		
		// secrete
		physical.modifyExtracellularQuantity(substanceID, 1000);

		
		// move cell no biology influence 
		
//		double speed1 = 5;
//		double[] deltaDirection = new double[] {0,0,0};
		//direction = add(direction, deltaDirection);
//		direction = normalize(direction);
//		super.cellElement.move(speed1, direction);
		
	
		
//		double concentration = physical.getExtracellularConcentration(substanceID);
		
		/*if(ecm.getRandomDouble()<concentration*branchingFactor){
			((NeuriteElement)cellElement).bifurcate();
		}*/
	}
	
	public static void main(String[] args) {
		ini.cx3d.utilities.SystemUtilities.tic();
		
		
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(0L);
		
		
		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}
		
//		// set the rectangle for ROI
//		Rectangle smallWindowRectangle = new Rectangle();
//		smallWindowRectangle.x = 100;
//		smallWindowRectangle.y = 100;
//		smallWindowRectangle.width = 320; 
//		smallWindowRectangle.height = 320;
		
//		Scheduler.simulateOneStep();
//		ecm.view.smallWindowRectangle = smallWindowRectangle;
		Substance yellowSubstance = new Substance("Yellow",70, 0.05);
		Substance violetSubstance = new Substance("Violet",70, 0.05);
		ecm.addNewSubstanceTemplate(yellowSubstance);
		ecm.addNewSubstanceTemplate(violetSubstance);
		for (int i = 0; i < 400; i++) {	
			ecm.getPhysicalNodeInstance(randomNoise(700,3));
		} 
		for(int i = 0; i<1; i++){
			Cell c = CellFactory.getCellInstance(new double[] {0.0,0.0,0.0});
			c.getSomaElement().addLocalBiologyModule(new SomaClusteringJona("Yellow"));
			c.setColorForAllPhysicalObjects(Param.X_SOLID_YELLOW);
			NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
			neurite.getPhysicalCylinder().setDiameter(0.2);
			neurite.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite1 = c.getSomaElement().extendNewNeurite();
			neurite1.getPhysicalCylinder().setDiameter(0.2);
			neurite1.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite11 = c.getSomaElement().extendNewNeurite();
			neurite11.getPhysicalCylinder().setDiameter(0.2);
			neurite11.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite111 = c.getSomaElement().extendNewNeurite();
			neurite111.getPhysicalCylinder().setDiameter(0.2);
			neurite111.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite1111 = c.getSomaElement().extendNewNeurite();
			neurite1111.getPhysicalCylinder().setDiameter(0.2);
			neurite1111.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			
			
			/*Cell c1 = CellFactory.getCellInstance(new double[] {50,-50,200});
			c1.getSomaElement().addLocalBiologyModule(new SomaClustering("Yellow"));
			c1.setColorForAllPhysicalObjects(Param.X_SOLID_YELLOW);
			NeuriteElement neurite2 = c1.getSomaElement().extendNewNeurite();
			neurite2.getPhysicalCylinder().setDiameter(2.0);
			neurite2.addLocalBiologyModule(new SomaClustering("Violet"));
			NeuriteElement neurite21 = c1.getSomaElement().extendNewNeurite();
			neurite1.getPhysicalCylinder().setDiameter(2.0);
			neurite1.addLocalBiologyModule(new SomaClustering("Violet"));
			NeuriteElement neurite211 = c1.getSomaElement().extendNewNeurite();
			neurite211.getPhysicalCylinder().setDiameter(2.0);
			neurite211.addLocalBiologyModule(new SomaClustering("Violet"));
			NeuriteElement neurite2111 = c1.getSomaElement().extendNewNeurite();
			neurite2111.getPhysicalCylinder().setDiameter(2.0);
			neurite2111.addLocalBiologyModule(new SomaClustering("Violet"));
			NeuriteElement neurite21111 = c1.getSomaElement().extendNewNeurite();
			neurite21111.getPhysicalCylinder().setDiameter(2.0);
			neurite21111.addLocalBiologyModule(new SomaClustering("Violet"));*/
			
			
		}
		for(int i = 0; i<1; i++){
			Cell c = CellFactory.getCellInstance(new double[] {-200,-200,0.0}); // cell location
			c.getSomaElement().addLocalBiologyModule(new SomaClusteringJona("Violet")); // cell Substance
			c.setColorForAllPhysicalObjects(Param.X_SOLID_VIOLET); // cell color 
			NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
			neurite.getPhysicalCylinder().setDiameter(0.2);
			neurite.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite1 = c.getSomaElement().extendNewNeurite();
			neurite1.getPhysicalCylinder().setDiameter(0.2);
			neurite1.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			NeuriteElement neurite11 = c.getSomaElement().extendNewNeurite();
			neurite11.getPhysicalCylinder().setDiameter(0.2);
			neurite11.addLocalBiologyModule(new SomaClusteringJona("Violet"));
			
			
			
			/*Cell c1 = CellFactory.getCellInstance(new double[] {200,-200,0.0}); // cell location
			c1.getSomaElement().addLocalBiologyModule(new SomaClustering("Violet")); // cell Substance
			c1.setColorForAllPhysicalObjects(Param.X_SOLID_VIOLET); // cell color */
		}
		Scheduler.setPrintCurrentECMTime(false);
		
		ini.cx3d.utilities.SystemUtilities.tacAndTic(); // set limited run time
		for (int i = 0; i < 100000; i++) {
			Scheduler.simulateOneStep();
			if(i%100==0){
				System.out.print("time step "+i+", time = ");
				ini.cx3d.utilities.SystemUtilities.tacAndTic();
			}
		}
		
	}
}
