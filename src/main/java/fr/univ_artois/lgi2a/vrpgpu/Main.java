/*
 *   Copyright or © or Copr. LGI2A
 *
 *   LGI2A - Laboratoire de Genie Informatique et d'Automatique de l'Artois - EA 3926
 *   Faculte des Sciences Appliquees
 *   Technoparc Futura
 *   62400 - BETHUNE Cedex
 *   http://www.lgi2a.univ-artois.fr/
 *
 *   Email: sohaib.lafifi@univ-artois.fr
 *
 *   Contributors:
 *   	Sohaib LAFIFI
 *
 *   This software is a computer program whose purpose is to implement some
 *   routing algorithms on GPU.
 *
 *   This software is governed by the CeCILL-B license under French law and
 *   abiding by the rules of distribution of free software.  You can  use,
 *   modify and/ or redistribute the software under the terms of the CeCILL-B
 *   license as circulated by CEA, CNRS and INRIA at the following URL
 *   "http://www.cecill.info".
 *
 *   As a counterpart to the access to the source code and  rights to copy,
 *   modify and redistribute granted by the license, users are provided only
 *   with a limited warranty  and the software's author,  the holder of the
 *   economic rights,  and the successive licensors  have only  limited
 *   liability.
 *
 *   In this respect, the user's attention is drawn to the risks associated
 *   with loading,  using,  modifying and/or developing or reproducing the
 *   software by the user in light of its specific status of free software,
 *   that may mean  that it is complicated to manipulate,  and  that  also
 *   therefore means  that it is reserved for developers  and  experienced
 *   professionals having in-depth computer knowledge. Users are therefore
 *   encouraged to load and test the software's suitability as regards their
 *   requirements in conditions enabling the security of their systems and/or
 *   data to be ensured and,  more generally, to use and operate it in the
 *   same conditions as regards security.
 *
 *   The fact that you are presently reading this means that you have had
 *   knowledge of the CeCILL-B license and that you accept its terms.
 *
 *
 */

package fr.univ_artois.lgi2a.vrpgpu;

import com.aparapi.internal.kernel.KernelManager;
import fr.univ_artois.lgi2a.vrpgpu.data.Problem;
import fr.univ_artois.lgi2a.vrpgpu.solvers.GASolver;

public class Main {
    /**
     * Main function
     *
     * @param args, args[0] is the data file path
     */
    public static void main(String[] args) {
        System.setProperty("com.amd.aparapi.dumpProfilesOnExit", "true");
        System.setProperty("com.amd.aparapi.enableProfiling", "true");
        System.setProperty("com.amd.aparapi.enableVerboseJNI", "true");
        System.setProperty("com.amd.aparapi.dumpFlags", "true");
        System.setProperty("com.amd.aparapi.enableShowGeneratedOpenCL", "true");
        System.setProperty("com.amd.aparapi.enableVerboseJNIOpenCLResourceTracking", "true");
        System.setProperty("com.amd.aparapi.enableExecutionModeReporting", "true");
        System.out.println(KernelManager.instance().bestDevice());
        Problem problem = new Problem();
        problem.read(args[0]);
        GASolver solver = new GASolver(problem);
        solver.solve();
        System.out.println("Solution " + solver.getCost() + " in " + solver.getCpuTime() + " sec");
    }
}
