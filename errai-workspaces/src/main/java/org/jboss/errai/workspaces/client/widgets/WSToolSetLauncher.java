/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.widgets.client.WSLaunchButton;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.api.ResourceFactory;
import org.jboss.errai.workspaces.client.api.Tool;
import org.jboss.errai.workspaces.client.api.ToolSet;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;
import org.jboss.errai.workspaces.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;


/**
 * A simple dock area to list and provide links to different tools.
 */
public class WSToolSetLauncher extends LayoutPanel
{

  ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);

  private String toolSetId = null;

  public WSToolSetLauncher(String id, final ToolSet toolSet)
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    setPadding(3);


     // widget, if available
    Widget w = toolSet.getWidget();
    this.toolSetId = id;

    if (w != null)
    {
      w.getElement().setId(toolSetId);
      this.add(w, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    }

    // tool links
    for (Tool t : toolSet.getAllProvidedTools()) {
      this.addLink(t.getName(), t);
    }

    this.getElement().setId(toolSetId);

  }

  public void addLink(final String name, final Tool tool)
  {
    ResourceFactory resourceFactory = GWT.create(ResourceFactory.class);
    ErraiImageBundle erraiImageBundle = GWT.create(ErraiImageBundle.class);
    ImageResource resource = resourceFactory.createImage(tool.getName()) != null ?
        resourceFactory.createImage(tool.getName()) : erraiImageBundle.application();

    WSLaunchButton button = new WSLaunchButton(resource, name);
    button.addClickListener(
        new ClickHandler()
        {          
          public void onClick(ClickEvent clickEvent)
          {
            Log.debug("ToolID: "+ tool.getId());

            MessageBuilder.createMessage()
                .toSubject(Workspace.SUBJECT)
                .command(LayoutCommands.ActivateTool)
                .with(LayoutParts.TOOL, tool.getId())
                .with(LayoutParts.TOOLSET, toolSetId)
                .noErrorHandling()
                .sendNowWith(ErraiBus.get());
          }
        }
    );
    this.add(button, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
  }
}
