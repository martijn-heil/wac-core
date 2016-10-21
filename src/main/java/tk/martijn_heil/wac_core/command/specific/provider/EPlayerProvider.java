/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.martijn_heil.wac_core.command.specific.provider;


public class EPlayerProvider //implements Provider<EPlayer>
{
//    private final Server server;
//    private Provider<Player> playerProvider;
//
//
//    public EPlayerProvider(Server server, Provider<Player> playerProvider)
//    {
//        this.server = server;
//        this.playerProvider = playerProvider;
//    }
//
//
//    @Override
//    public boolean isProvided()
//    {
//        return false;
//    }
//
//
//    @Nullable
//    @Override
//    public EPlayer get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException
//    {
//        Player player = playerProvider.get(commandArgs, list);
//        EPlayer ep = ElytraOptions.getEPlayer(player);
//        if(ep == null) throw new ArgumentParseException("Player not found.");
//        return ep;
//    }
//
//
//    @Override
//    public List<String> getSuggestions(String s)
//    {
//        List<String> eMatches = new ArrayList<>();
//        for (Player p : server.matchPlayer(s))
//        {
//            EPlayer ep = ElytraOptions.getEPlayer(p);
//            if(ep != null)
//            {
//                eMatches.add(ep.getPlayer().getName());
//            }
//        }
//
//        return eMatches;
//    }
}
