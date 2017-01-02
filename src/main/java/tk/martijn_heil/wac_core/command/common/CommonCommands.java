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

package tk.martijn_heil.wac_core.command.common;


import com.sk89q.intake.Command;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.parametric.annotation.Optional;
import org.bukkit.command.CommandSender;

public class CommonCommands
{
    private Dispatcher dispatcher;


    public CommonCommands()
    {

    }

    @Command(aliases = {"help", "?", "h"}, desc = "Show tk.martijn_heil.wac_core.command help", usage = "[tk.martijn_heil.wac_core.command=overview]")
    public void help(@Sender CommandSender sender, @Optional CommandCallable callable)
    {
        if(callable != null)
        {
            CommonUtils.sendCommandHelp(callable, sender);
        }
        else
        {
            CommonUtils.sendCommandHelp(dispatcher, sender);
        }
    }


    /**
     * This is a bit hacky, CommonCommands depends on the dispatcher it belongs to, but when we *have* to register
     * the CommonCommands, the dispatcher is not initialized yet. So we have to do late-initialization as you can see below
     *
     * @param dispatcher The dispatcher this belongs to.
     */
    public void lateInit(Dispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }
}
