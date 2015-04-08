(function () {
	var app = angular.module('myse', ['ngRoute']);

	app.config(
			function ($routeProvider, $locationProvider) {
				$routeProvider
						.when(
								"/search",
								{
									templateUrl: '/static/search.html',
									controller: 'SearchCtrl',
									controllerAs: 'search',
									reloadOnSearch: false
								}
						)
						.when(
								"/setup",
								{
									templateUrl: '/static/setup-list.html',
									controller: 'SetupListCtrl',
									controllerAs: 'setup'
								}
						)
						.when(
								"/setup/source/:sourceId/edit",
								{
									templateUrl: '/static/setup-edit.html',
									controller: 'SetupEditCtrl',
									controllerAs: 'setup'
								}
						)
						.otherwise(
								{
									redirectTo: '/search'
								}
						);
			}
	);

	app.controller('NavCtrl', [
		'$location',
		function ($location) {
			this.isSelected = function (page) {
				return $location.path().startsWith(page);
			};
			this.class = function (page) {
				return {
					active: this.isSelected(page)
				};
			};
		}
	]);
	app.controller('Version', ['$http', function ($http) {
			this.props = {};
			var ctrl = this;
			$http.get('/rest/version').success(function (data) {
				ctrl.props = data;
			});
		}]);

	app.controller('SearchCtrl', ['$http', '$location', function ($http, $location) {
			// CONTROLLER CODE
			var ctrl = this;
			this.response = {
				results: [],
				error: null
			};
			this.queryChanged = function () {
				$http.post('/rest/search', {'q': this.query}).success(
						function (data) {
							ctrl.response = data;
						}
				);
				if (this.query !== '' && this.query !== false) {
					$location.search('q', this.query);
				} else {
					$location.search('q', null);
				}
			};
			this.query = $location.search()['q'];
			if (this.query !== undefined) {
				this.queryChanged();
			}
		}
	]
			);

	app.controller(
			'SetupListCtrl',
			['$http',
				function ($http) {
					var ctrl = this;
					this.sources = [];
					this.fetchSources = function () {
						$http.get('/rest/setup/source/list').success(
								function (data) {
									ctrl.sources = data;
								});
					};

					this.fetchSources();
				}
			]);

	app.controller(
			'SetupEditCtrl',
			['$http', '$routeParams',
				function ($http, $routeParams) {
					var ctrl = this;
					this.sourceId = $routeParams.sourceId;
					this.props = {};
					this.descs = {};
					this.types = [];

					$http.get('/rest/setup/source/get?id=' + this.sourceId).success(
							function (data) {
								ctrl.props = data;
								
								ctrl.typeChanged();
							}
					);

					$http.get('/rest/setup/source/types').success(
							function (data) {
								ctrl.types = data;
							}
					);

					this.typeChanged = function () {
						$http.get('/rest/setup/source/desc?type=' + this.props._type).success(
								function (data) {
									ctrl.descs = data;
								}
						);
					};

					this.save = function () {
						$http.post('/rest/setup/source/edit', this.props).success(
								function (data) {

								}
						);
					};
				}
			]);

})();

$('#core').show();

// Source http://stackoverflow.com/a/646643/847202
if (typeof String.prototype.startsWith !== 'function') {
	// see below for better implementation!
	String.prototype.startsWith = function (str) {
		return this.indexOf(str) === 0;
	};
}