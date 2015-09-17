import React from 'react';
import PermissionsHeader from './permissions-header';
import Project from './project';

export default React.createClass({
  propTypes:{
    projects: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
    permissions: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
    permissionTemplates: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
    refresh: React.PropTypes.func.isRequired
  },

  render() {
    let projects = this.props.projects.map(p => {
      return <Project
          key={p.id}
          project={p}
          permissionTemplates={this.props.permissionTemplates}
          refresh={this.props.refresh}/>;
    });
    return (
        <table id="projects" className="table-list">
          <PermissionsHeader permissions={this.props.permissions}/>
          <tbody>{projects}</tbody>
        </table>
    );
  }
});
