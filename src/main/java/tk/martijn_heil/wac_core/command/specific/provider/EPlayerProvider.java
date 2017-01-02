/*
 * MIT License
 *
 * Copyright (c) 2016 Martijn Heil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
