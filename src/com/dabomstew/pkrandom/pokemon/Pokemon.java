package com.dabomstew.pkrandom.pokemon;

/*----------------------------------------------------------------------------*/
/*--  Pokemon.java - represents an individual Pokemon, and contains         --*/
/*--                 common Pokemon-related functions.                      --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.Species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Pokemon implements Comparable<Pokemon> {

    public String name;
    public int number;

    public String formeSuffix = "";
    public Pokemon baseForme = null;
    public int formeNumber = 0;
    public int cosmeticForms = 0;
    public int formeSpriteIndex = 0;
    public boolean actuallyCosmetic = false;
    public List<Integer> realCosmeticFormNumbers = new ArrayList<>();

    public Type primaryType, secondaryType;

    public int hp, attack, defense, spatk, spdef, speed, special;

    public int ability1, ability2, ability3;

    public int catchRate, expYield;

    public int guaranteedHeldItem, commonHeldItem, rareHeldItem, darkGrassHeldItem;

    public int genderRatio;

    public int frontSpritePointer, picDimensions;

    public int callRate;

    public ExpCurve growthCurve;

    public List<Evolution> evolutionsFrom = new ArrayList<>();
    public List<Evolution> evolutionsTo = new ArrayList<>();

    public List<MegaEvolution> megaEvolutionsFrom = new ArrayList<>();
    public List<MegaEvolution> megaEvolutionsTo = new ArrayList<>();

    protected List<Integer> shuffledStatsOrder;

    // A flag to use for things like recursive stats copying.
    // Must not rely on the state of this flag being preserved between calls.
    public boolean temporaryFlag;

    public Pokemon() {
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }

    public void shuffleStats(Random random) {
        Collections.shuffle(shuffledStatsOrder, random);
        applyShuffledOrderToStats();
    }
    
    public void copyShuffledStatsUpEvolution(Pokemon evolvesFrom) {
        // If stats were already shuffled once, un-shuffle them
        shuffledStatsOrder = Arrays.asList(
                shuffledStatsOrder.indexOf(0),
                shuffledStatsOrder.indexOf(1),
                shuffledStatsOrder.indexOf(2),
                shuffledStatsOrder.indexOf(3),
                shuffledStatsOrder.indexOf(4),
                shuffledStatsOrder.indexOf(5));
        applyShuffledOrderToStats();
        shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
        applyShuffledOrderToStats();
    }

    protected void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(hp, attack, defense, spatk, spdef, speed);

        // Copy in new stats
        hp = stats.get(shuffledStatsOrder.get(0));
        attack = stats.get(shuffledStatsOrder.get(1));
        defense = stats.get(shuffledStatsOrder.get(2));
        spatk = stats.get(shuffledStatsOrder.get(3));
        spdef = stats.get(shuffledStatsOrder.get(4));
        speed = stats.get(shuffledStatsOrder.get(5));
    }

    public void randomizeStatsWithinBST(Random random) {
        if (number == Species.shedinja) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst = bst() - 51;

            // Make weightings
            double atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        } else {
            // Minimum 20 HP, 10 everything else
            int bst = bst() - 70;

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            hp = (int) Math.max(1, Math.round(hpW / totW * bst)) + 20;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }

    }

    public void randomizeStatsLogNorm(Random random, Settings settings) {
        // Get a new bst, and a "role" (specialist vs generalist)
        int new_bst = pickNewBSTLogNorm(random, settings);
        distributeStatsLogNorm(random, new_bst);
    }

    public void randomizeStatsBoss(Random random, Settings settings) {
        //TODO: Eventually I'd like this range to be controllable from a setting
        int bst = 800 + random.nextInt(101);
        distributeStatsLogNorm(random, bst);
    }

    private void distributeStatsLogNorm(Random random, int new_bst) {
        double role_modifier = random.nextDouble();
//        System.out.printf("%15s %4.3f\n", name, role_modifier);
        double atkW = getStatRatio(random, role_modifier), defW = getStatRatio(random, role_modifier);
        double spaW = getStatRatio(random, role_modifier), spdW = getStatRatio(random, role_modifier);
        double speW = getStatRatio(random, role_modifier), hpW  = getStatRatio(random, role_modifier);

        if(number == Species.shedinja){
            new_bst *= 5.0/6;
            double[] weightsArr = {atkW, defW, spaW, spdW, speW};
            double totW = atkW + defW + spaW + spdW + speW;

            // Because shedinja is special
            hp = 1;
            attack = Math.max(10, Math.min(255, (int) Math.round(new_bst * atkW/totW)));
            defense = Math.max(10, Math.min(255, (int) Math.round(new_bst * defW/totW)));
            spatk = Math.max(10, Math.min(255, (int) Math.round(new_bst * spaW/totW)));
            spdef = Math.max(10, Math.min(255, (int) Math.round(new_bst * spdW/totW)));
            speed = Math.max(10, Math.min(255, (int) Math.round(new_bst * speW/totW)));

            int loops = 0;
            while(new_bst - bst() > 10 && loops < 5) {
                int diff = new_bst - bst();
                totW = 0;
                int[] statArr = {attack, defense, spatk, spdef, speed};
                for(int i = 0; i < statArr.length; i++) {
                    if (statArr[i] < 255) {
                        totW += weightsArr[i];
                    }
                }

                attack = Math.min(255, (int) Math.round(attack + atkW/totW*diff));
                defense = Math.min(255, (int) Math.round(defense + defW/totW*diff));
                spatk = Math.min(255, (int) Math.round(spatk + spaW/totW*diff));
                spdef = Math.min(255, (int) Math.round(spdef + spdW/totW*diff));
                speed = Math.min(255, (int) Math.round(speed + speW/totW*diff));

                loops++;
            }
        } else {
            double[] weightsArr = {hpW, atkW, defW, spaW, spdW, speW};
            double totW = hpW + atkW + defW + spaW + spdW + speW;

            // Because shedinja is special
            hp = Math.max(20, Math.min(255, (int) Math.round(new_bst * hpW/totW)));
            attack = Math.max(10, Math.min(255, (int) Math.round(new_bst * atkW/totW)));
            defense = Math.max(10, Math.min(255, (int) Math.round(new_bst * defW/totW)));
            spatk = Math.max(10, Math.min(255, (int) Math.round(new_bst * spaW/totW)));
            spdef = Math.max(10, Math.min(255, (int) Math.round(new_bst * spdW/totW)));
            speed = Math.max(10, Math.min(255, (int) Math.round(new_bst * speW/totW)));

            int loops = 0;
            while(new_bst - bst() > 10 && loops < 5) {
                int diff = new_bst - bst();
                totW = 0;
                int[] statArr = {hp, attack, defense, spatk, spdef, speed};
                for (int i = 0; i < statArr.length; i++) {
                    if (statArr[i] < 255) {
                        totW += weightsArr[i];
                    }
                }

                hp = Math.min(255, (int) Math.round(hp + hpW / totW * diff));
                attack = Math.min(255, (int) Math.round(attack + atkW / totW * diff));
                defense = Math.min(255, (int) Math.round(defense + defW / totW * diff));
                spatk = Math.min(255, (int) Math.round(spatk + spaW / totW * diff));
                spdef = Math.min(255, (int) Math.round(spdef + spdW / totW * diff));
                speed = Math.min(255, (int) Math.round(speed + speW / totW * diff));

                loops++;
            }
        }
    }

    /**
     * Picks a new BST total, depending on whether this is a Mega, fully evolved, or neither
     * @return A BST depending on what kind of pokemon and settings are used
     */
    private int pickNewBSTLogNorm(Random random, Settings settings) {
        int res;

        // If we don't care about following evolutions, just do it totally randomly
        if(!settings.isBaseStatsFollowEvolutions()) {
            res = generateLogNormBST(random, settings);
        }
        // If it's a mega evo, just add some bst onto its base forme
        else if(megaEvolutionsTo.size() > 0) {
            res = megaEvolutionsTo.get(0).from.bst() + random.nextInt(151);
        }
        // If it's any other alt forme, it should have the same bst
        else if(baseForme != null) {
            res = baseForme.bst();
        }
        // If it's fully evolved, pick its bst following log norm distribution
        else if (evolutionsFrom.size() == 0) {
            res = generateLogNormBST(random, settings);
        }
        // If it's not fully evolved, and we're following evos, it's kinda complicated
        else {
            //TODO: Maybe inline or extract a function?
            int evMin = Collections.min(
                    evolutionsFrom.stream()
                            .map(evoFrom -> evoFrom.to.bst())
                            .collect(Collectors.toList()));
            int steps = getEvToDepth() + 2;
            int maxPossibleValue = Math.min(evMin, 600);
            int minPossibleValue = 180;
            int stepSize = (maxPossibleValue - minPossibleValue)/steps;
            int midPoint = minPossibleValue + (steps - 1) * stepSize;
            int range = ((maxPossibleValue - midPoint) * 5) / 6;

            int upperBound = midPoint + range;
            int lowerBound = midPoint - range;

            int diff = Math.max(upperBound - lowerBound, 1);
            res = random.nextInt(diff) + lowerBound;
            if(res < 120) {
                System.out.println(res);
            }
        }
        return res;
    }

    private int generateLogNormBST(Random random, Settings settings) {
        /*
        Depending on whether we want bsts to follow evolutions, we want to use slightly different distributions

        The Follow Evolutions distribution was derived by analyzing the vanilla pokemon bst distribution

        The Non-follow evolutions distribution is a shot in the dark, because we don't really have a good baseline
         */

        // TODO: Extract these as parameters, then just manually input them when we call the function

        // Minimum value (it is impossible to return something lower than this)
        int minimum;

        // Half of all values will be lower/higher than this (on average)
        int median;

        // Represents a remarkably high value we would expect to see in the distribution
        int high_value;

        // How often we want to see something greater than or equal to that remarkably high value (as a z-score)
        double z_score;

        if(settings.isBaseStatsFollowEvolutions()) {
            minimum = 300;
            median = 500;
            high_value = 700;
            z_score = 2.1701; // this z score corresponds to 1.5%.
        } else {
            minimum = 150;
            median = 450;
            high_value = 700;
            z_score = 2.5758;// this z score corresponds to .5%.
        }
        double norm = random.nextGaussian();

        return (int) Math.round(minimum + (median - minimum) * Math.pow((high_value - minimum + 0.0)/(median - minimum + 0.0), (norm / z_score)));
    }

    private double getStatRatio(Random random, double roleModifier) {
        double x = random.nextDouble();
        double maxRatio = 15;
        double offset = 1.0/(maxRatio - 1);
        return (1.0/(2*Math.PI)) * (2*roleModifier - 1) * Math.sin(2*Math.PI*x) + x + offset;
    }

    public void copyRandomizedStatsUpEvolution(Pokemon evolvesFrom) {
        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstRatio = ourBST / theirBST;

        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)));
    }

    public void assignNewStatsForEvolution(Pokemon evolvesFrom, Random random) {

        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstDiff = ourBST - theirBST;

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double spaDiff = Math.round((spaW / totW) * bstDiff);
        double spdDiff = Math.round((spdW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        hp = (int) Math.min(255, Math.max(1, evolvesFrom.hp + hpDiff));
        attack = (int) Math.min(255, Math.max(1, evolvesFrom.attack + atkDiff));
        defense = (int) Math.min(255, Math.max(1, evolvesFrom.defense + defDiff));
        speed = (int) Math.min(255, Math.max(1, evolvesFrom.speed + speDiff));
        spatk = (int) Math.min(255, Math.max(1, evolvesFrom.spatk + spaDiff));
        spdef = (int) Math.min(255, Math.max(1, evolvesFrom.spdef + spdDiff));
    }

    public int bst() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    public int bstForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (number == Species.shedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return hp + attack + defense + spatk + spdef + speed;
        }
    }

    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    public int getBaseNumber() {
        Pokemon base = this;
        while (base.baseForme != null) {
            base = base.baseForme;
        }
        return base.number;
    }

    public int getEvFromDepth() {
        int max_depth = 0;
        for(Evolution evFrom: evolutionsFrom) {
            max_depth = Math.max(max_depth, 1 + evFrom.to.getEvFromDepth());
        }
        return max_depth;
    }

    public int getEvToDepth() {
        int max_depth = 0;
        // Might be a reduction formula to do this, but I can't be bothered right now
        for(Evolution evTo: evolutionsTo) {
            max_depth = Math.max(max_depth, 1 + evTo.from.getEvToDepth());
        }
        return max_depth;
    }

    public void copyBaseFormeBaseStats(Pokemon baseForme) {
        hp = baseForme.hp;
        attack = baseForme.attack;
        defense = baseForme.defense;
        speed = baseForme.speed;
        spatk = baseForme.spatk;
        spdef = baseForme.spdef;
    }

    public void copyBaseFormeAbilities(Pokemon baseForme) {
        ability1 = baseForme.ability1;
        ability2 = baseForme.ability2;
        ability3 = baseForme.ability3;
    }

    public void copyBaseFormeEvolutions(Pokemon baseForme) {
        evolutionsFrom = baseForme.evolutionsFrom;
    }

    public int getSpriteIndex() {
        return formeNumber == 0 ? number : formeSpriteIndex + formeNumber - 1;
    }

    public String fullName() {
        return name + formeSuffix;
    }

    @Override
    public String toString() {
        return "Pokemon [name=" + name + formeSuffix + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack + ", defense=" + defense
                + ", spatk=" + spatk + ", spdef=" + spdef + ", speed=" + speed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pokemon other = (Pokemon) obj;
        return number == other.number;
    }

    @Override
    public int compareTo(Pokemon o) {
        return number - o.number;
    }

    private static final List<Integer> legendaries = Arrays.asList(Species.articuno, Species.zapdos, Species.moltres,
            Species.mewtwo, Species.mew, Species.raikou, Species.entei, Species.suicune, Species.lugia, Species.hoOh,
            Species.celebi, Species.regirock, Species.regice, Species.registeel, Species.latias, Species.latios,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.jirachi, Species.deoxys, Species.uxie,
            Species.mesprit, Species.azelf, Species.dialga, Species.palkia, Species.heatran, Species.regigigas,
            Species.giratina, Species.cresselia, Species.phione, Species.manaphy, Species.darkrai, Species.shaymin,
            Species.arceus, Species.victini, Species.cobalion, Species.terrakion, Species.virizion, Species.tornadus,
            Species.thundurus, Species.reshiram, Species.zekrom, Species.landorus, Species.kyurem, Species.keldeo,
            Species.meloetta, Species.genesect, Species.xerneas, Species.yveltal, Species.zygarde, Species.diancie,
            Species.hoopa, Species.volcanion, Species.typeNull, Species.silvally, Species.tapuKoko, Species.tapuLele,
            Species.tapuBulu, Species.tapuFini, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala,
            Species.necrozma, Species.magearna, Species.marshadow, Species.zeraora);

    private static final List<Integer> strongLegendaries = Arrays.asList(Species.mewtwo, Species.lugia, Species.hoOh,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.dialga, Species.palkia, Species.regigigas,
            Species.giratina, Species.arceus, Species.reshiram, Species.zekrom, Species.kyurem, Species.xerneas,
            Species.yveltal, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala);

    private static final List<Integer> ultraBeasts = Arrays.asList(Species.nihilego, Species.buzzwole, Species.pheromosa,
            Species.xurkitree, Species.celesteela, Species.kartana, Species.guzzlord, Species.poipole, Species.naganadel,
            Species.stakataka, Species.blacephalon);

    public boolean isLegendary() {
        return formeNumber == 0 ? legendaries.contains(this.number) : legendaries.contains(this.baseForme.number);
    }

    public boolean isStrongLegendary() {
        return formeNumber == 0 ? strongLegendaries.contains(this.number) : strongLegendaries.contains(this.baseForme.number);
    }

    // This method can only be used in contexts where alt formes are NOT involved; otherwise, some alt formes
    // will be considered as Ultra Beasts in SM.
    // In contexts where formes are involved, use "if (ultraBeastList.contains(...))" instead,
    // assuming "checkPokemonRestrictions" has been used at some point beforehand.
    public boolean isUltraBeast() {
        return ultraBeasts.contains(this.number);
    }

    public int getCosmeticFormNumber(int num) {
        return realCosmeticFormNumbers.isEmpty() ? num : realCosmeticFormNumbers.get(num);
    }
}
