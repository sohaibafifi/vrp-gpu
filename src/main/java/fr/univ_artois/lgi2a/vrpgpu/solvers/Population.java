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

package fr.univ_artois.lgi2a.vrpgpu.solvers;

import fr.univ_artois.lgi2a.vrpgpu.data.Chromosome;
import fr.univ_artois.lgi2a.vrpgpu.data.Client;
import fr.univ_artois.lgi2a.vrpgpu.data.Problem;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Population {
    private List<Chromosome> chromosomes;
    private Problem problem;
    private int maxSize;

    public Population(Problem problem) {
        this.problem = problem;
        this.maxSize = problem.getNbClients();
        this.chromosomes = new ArrayList<Chromosome>(this.maxSize);

        while (this.chromosomes.size() < this.maxSize) {

                this.insert(new Chromosome(this.problem));
        }

    }

    public Chromosome getBestChromsome() {
        return chromosomes.get(0);
    }

    public Chromosome getWorstChromsome() {
        return chromosomes.get(this.chromosomes.size());
    }

    public boolean insert(Chromosome chromosome) {
        int i = 0;
        for (; i < this.chromosomes.size(); i++) {

            if (this.chromosomes.get(i).decode() > chromosome.decode()) {
                this.chromosomes.add(i, chromosome);
                if (this.chromosomes.size() > this.maxSize) this.chromosomes.remove(this.chromosomes.size() - 1);
                return true;
            }

            if (this.chromosomes.get(i).decode().compareTo(chromosome.decode()) == 0) {
                this.chromosomes.set(i, chromosome);
                return true;
            }
        }
        if (i < this.maxSize) {
            this.chromosomes.add(chromosome);
            return true;
        }
        return false;
    }

    public boolean evolve() {

        Random rand = new Random(System.currentTimeMillis());
        SortedSet<Chromosome> parents = new TreeSet<>(Comparator.comparingDouble(Chromosome::decode));
        List<Integer> index = IntStream.range(0, this.chromosomes.size()).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(index, rand);
        for (int i = 0; i < 4; i++) {
            parents.add(chromosomes.get(index.get(i)));
        }
        parents.remove(parents.last());
        parents.remove(parents.last());


        Chromosome firstParent = parents.first();
        Chromosome secondParent = parents.last();
        int point1 = rand.nextInt(Integer.MAX_VALUE) % firstParent.getSequence().size();
        int point2 = 0;
        do {
            point2 = rand.nextInt(Integer.MAX_VALUE) % firstParent.getSequence().size();
        } while (point1 == point2);
        if (point2 < point1) {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }
        List<Client> child_sequence = new ArrayList<>();
        for (int i = 0; i < firstParent.getSequence().size(); i++)
            child_sequence.add(null);

        List<Boolean> inserted = IntStream.range(0, firstParent.getSequence().size() + 1).mapToObj(i -> false).collect(Collectors.toList());
        for (int i = 0; i < child_sequence.size(); i++)
            if (i < point1 || i >= point2) {
                child_sequence.set(i, firstParent.getSequence().get(i));
                inserted.set(child_sequence.get(i).getId(), true);
            }
        for (int j = 0; j < secondParent.getSequence().size(); j++) {
            for (int i = 0; i < child_sequence.size(); i++) {
                if (!inserted.get(secondParent.getSequence().get(j).getId())
                        &&
                        child_sequence.get(i) == null) {
                    child_sequence.set(i, secondParent.getSequence().get(j));
                    inserted.set(child_sequence.get(i).getId(), true);
                }
            }
        }

        Chromosome child = new Chromosome(problem, child_sequence);
        return this.insert(child);
    }


    public void print() {
        for (int i = 0; i < chromosomes.size(); i++) {
            System.out.print(i + " : ");
            chromosomes.get(i).print();
            System.out.println();
        }
    }

}
