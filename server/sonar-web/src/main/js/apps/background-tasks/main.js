import _ from 'underscore';
import React from 'react';
import {getQueue, getActivity, cancelTask} from '../../api/ce';
import {STATUSES, CURRENTS} from './constants';
import Header from './header';
import Stats from './stats';
import Search from './search';
import Tasks from './tasks';
import ListFooter from '../../components/shared/list-footer';

const PAGE_SIZE = 200;

export default React.createClass({
  getInitialState() {
    return {
      queue: [],
      activity: [],
      activityTotal: 0,
      activityPage: 1,
      pendingCount: 0,
      failuresCount: 0,
      statusFilter: STATUSES.ALL,
      currentsFilter: CURRENTS.ALL
    };
  },

  componentDidMount() {
    this.requestData();
  },

  getCurrentFilters() {
    let filters = {};
    if (this.state.statusFilter !== STATUSES.ALL) {
      filters.status = this.state.statusFilter;
    }
    if (this.state.currentsFilter !== STATUSES.ALL) {
      filters.onlyCurrents = true;
    }
    return filters;
  },

  requestData() {
    this.requestQueue();
    this.requestActivity();
  },

  requestQueue() {
    if (!Object.keys(this.getCurrentFilters()).length) {
      getQueue().done(queue => {
        this.setState({
          queue: this.orderTasks(queue.tasks),
          pendingCount: this.countPending(queue.tasks),
          inProgressDuration: this.getInProgressDuration(queue.tasks)
        });
      });
    } else {
      this.setState({ queue: [] });
    }
  },

  requestActivity() {
    let filters = _.extend(this.getCurrentFilters(), { p: this.state.activityPage, ps: PAGE_SIZE });
    getActivity(filters).done(activity => {
      let newActivity = activity.paging.pageIndex === 1 ?
          activity.tasks : [].concat(this.state.activity, activity.tasks);
      this.setState({
        activity: this.orderTasks(newActivity),
        activityTotal: activity.paging.total,
        activityPage: activity.paging.pageIndex
      });
    });
  },

  countPending(tasks) {
    return _.where(tasks, { status: STATUSES.PENDING }).length;
  },

  orderTasks(tasks) {
    return _.sortBy(tasks, task => {
      return -moment(task.submittedAt).unix();
    });
  },

  getInProgressDuration(tasks) {
    let taskInProgress = _.findWhere(tasks, { status: STATUSES.IN_PROGRESS });
    return taskInProgress ? moment().diff(taskInProgress.startedAt) : null;
  },

  onStatusChange(newStatus) {
    this.setState({ statusFilter: newStatus, activityPage: 1 }, this.requestData);
  },

  onCurrentsChange(newCurrents) {
    this.setState({ currentsFilter: newCurrents, activityPage: 1 }, this.requestData);
  },

  loadMore() {
    this.setState({ activityPage: this.state.activityPage + 1 }, this.requestActivity);
  },

  onTaskCanceled(task) {
    cancelTask(task.id).done(data => {
      _.extend(task, data.task);
      this.forceUpdate();
    });
  },

  render() {
    return (
        <div className="page">
          <Header/>
          <Stats {...this.state}/>
          <Search {...this.state} onStatusChange={this.onStatusChange} onCurrentsChange={this.onCurrentsChange}/>
          <Tasks tasks={[].concat(this.state.queue, this.state.activity)} onTaskCanceled={this.onTaskCanceled}/>
          <ListFooter count={this.state.queue.length + this.state.activity.length}
                      total={this.state.queue.length + this.state.activityTotal}
                      loadMore={this.loadMore}/>
        </div>
    );
  }
});
